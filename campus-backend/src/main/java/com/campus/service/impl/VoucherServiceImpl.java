package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.common.BusinessException;
import com.campus.common.ErrorCode;
import com.campus.common.PageResult;
import com.campus.entity.SeckillVoucher;
import com.campus.entity.Voucher;
import com.campus.interceptor.UserHolder;
import com.campus.mapper.SeckillVoucherMapper;
import com.campus.mapper.VoucherMapper;
import com.campus.service.VoucherService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class VoucherServiceImpl extends ServiceImpl<VoucherMapper, Voucher> implements VoucherService {
    
    private final StringRedisTemplate stringRedisTemplate;
    private final SeckillVoucherMapper seckillVoucherMapper;
    
    /** Lua 脚本 */
    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;
    
    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("lua/seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }
    
    /** Stream 队列名 */
    private static final String STREAM_KEY = "stream:voucher:order";
    /** 秒杀券信息缓存 Key 前缀（Redis Hash） */
    private static final String SECKILL_INFO_PREFIX = "seckill:info:";
    /** 时间格式化 */
    private static final DateTimeFormatter DT_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    public VoucherServiceImpl(StringRedisTemplate stringRedisTemplate,
                              SeckillVoucherMapper seckillVoucherMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.seckillVoucherMapper = seckillVoucherMapper;
    }
    
    /**
     * 应用启动时：预热所有秒杀券的库存 + 优惠券详细信息到 Redis
     * 
     * 预热内容：
     * 1. seckill:stock:{voucherId} → 库存（String）
     * 2. seckill:info:{voucherId} → 优惠券信息（Hash: shopId, type, status, payValue, actualValue, title, subTitle, rules, beginTime, endTime）
     */
    @PostConstruct
    public void initSeckillVouchers() {
        try {
            // 查询所有秒杀券库存记录
            LambdaQueryWrapper<SeckillVoucher> wrapper = new LambdaQueryWrapper<>();
            List<SeckillVoucher> seckillVouchers = seckillVoucherMapper.selectList(wrapper);
            
            int count = 0;
            for (SeckillVoucher sv : seckillVouchers) {
                Long voucherId = sv.getVoucherId();
                String stockKey = "seckill:stock:" + voucherId;
                String infoKey = SECKILL_INFO_PREFIX + voucherId;
                
                // 1. 预热库存（如果不存在）
                if (!Boolean.TRUE.equals(stringRedisTemplate.hasKey(stockKey))) {
                    stringRedisTemplate.opsForValue().set(stockKey, String.valueOf(sv.getStock()));
                    log.info("预热库存：voucherId={}, stock={}", voucherId, sv.getStock());
                }
                
                // 2. 预热优惠券详细信息（如果不存在）
                if (!Boolean.TRUE.equals(stringRedisTemplate.hasKey(infoKey))) {
                    Voucher voucher = this.getById(voucherId);
                    if (voucher != null) {
                        cacheVoucherInfo(voucher);
                        log.info("预热秒杀券信息：voucherId={}, title={}", voucherId, voucher.getTitle());
                        count++;
                    }
                }
            }
            
            log.info("✅ 秒杀券预热完成，共预热 {} 个秒杀券（总 {} 个）", count, seckillVouchers.size());
        } catch (Exception e) {
            log.error("秒杀券预热失败", e);
        }
    }
    
    /**
     * 将优惠券信息缓存到 Redis Hash
     * Key: seckill:info:{voucherId}
     * Fields: shopId, type, status, payValue, actualValue, title, subTitle, rules, beginTime, endTime
     */
    private void cacheVoucherInfo(Voucher voucher) {
        String infoKey = SECKILL_INFO_PREFIX + voucher.getId();
        Map<String, String> map = new HashMap<>();
        map.put("shopId", String.valueOf(voucher.getShopId()));
        map.put("type", String.valueOf(voucher.getType()));
        map.put("status", String.valueOf(voucher.getStatus()));
        map.put("payValue", String.valueOf(voucher.getPayValue()));
        map.put("actualValue", String.valueOf(voucher.getActualValue()));
        map.put("title", voucher.getTitle() != null ? voucher.getTitle() : "");
        map.put("subTitle", voucher.getSubTitle() != null ? voucher.getSubTitle() : "");
        map.put("rules", voucher.getRules() != null ? voucher.getRules() : "");
        if (voucher.getBeginTime() != null) {
            map.put("beginTime", voucher.getBeginTime().format(DT_FORMATTER));
        }
        if (voucher.getEndTime() != null) {
            map.put("endTime", voucher.getEndTime().format(DT_FORMATTER));
        }
        stringRedisTemplate.opsForHash().putAll(infoKey, map);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(Voucher voucher) {
        // 设置默认值
        if (voucher.getStatus() == null) {
            voucher.setStatus(2); // 默认下架
        }
        if (voucher.getType() == null) {
            voucher.setType(0); // 默认普通券
        }
        
        // 验证参数
        validateVoucher(voucher);
        
        this.save(voucher);
        log.info("创建优惠券成功：{}", voucher.getTitle());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Voucher voucher) {
        Voucher existing = this.getById(voucher.getId());
        if (existing == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "优惠券不存在");
        }
        
        // 验证参数
        validateVoucher(voucher);
        
        this.updateById(voucher);
        
        // 同步更新 Redis 缓存
        String infoKey = SECKILL_INFO_PREFIX + voucher.getId();
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(infoKey))) {
            Voucher updated = this.getById(voucher.getId());
            cacheVoucherInfo(updated);
        }
        
        log.info("修改优惠券成功：id={}", voucher.getId());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Voucher voucher = this.getById(id);
        if (voucher == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "优惠券不存在");
        }
        
        // 如果优惠券已上架，不允许删除
        if (voucher.getStatus() == 1) {
            throw new BusinessException(ErrorCode.CONFLICT, "优惠券已上架，请先下架再删除");
        }
        
        this.removeById(id);
        
        // 清理 Redis 缓存
        stringRedisTemplate.delete(SECKILL_INFO_PREFIX + id);
        stringRedisTemplate.delete("seckill:stock:" + id);
        stringRedisTemplate.delete("seckill:order:" + id);
        
        log.info("删除优惠券成功：{}", voucher.getTitle());
    }
    
    @Override
    public PageResult<Voucher> pageForAdmin(int page, int size, Long shopId, Integer type, Integer status) {
        Page<Voucher> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Voucher> wrapper = new LambdaQueryWrapper<Voucher>()
                .eq(shopId != null, Voucher::getShopId, shopId)
                .eq(type != null, Voucher::getType, type)
                .eq(status != null, Voucher::getStatus, status)
                .orderByDesc(Voucher::getCreateTime);
        
        Page<Voucher> result = this.page(pageParam, wrapper);
        
        // 自动更新过期状态
        updateExpiredVouchers(result.getRecords());
        
        return new PageResult<>(
                result.getRecords(),
                result.getTotal(),
                (int) result.getCurrent(),
                (int) result.getSize()
        );
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        Voucher voucher = this.getById(id);
        if (voucher == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "优惠券不存在");
        }
        
        // 验证状态
        if (status != 1 && status != 2) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "状态值无效，只能为1(上架)或2(下架)");
        }
        
        // 上架时检查库存
        if (status == 1 && voucher.getStock() <= 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "库存不足，无法上架");
        }
        
        // 上架时检查秒杀券时间
        if (status == 1 && voucher.getType() == 1) {
            LocalDateTime now = LocalDateTime.now();
            if (voucher.getEndTime() != null && voucher.getEndTime().isBefore(now)) {
                throw new BusinessException(ErrorCode.CONFLICT, "优惠券已过期，无法上架");
            }
        }
        
        voucher.setStatus(status);
        this.updateById(voucher);
        
        // 同步更新 Redis 缓存中的状态
        String infoKey = SECKILL_INFO_PREFIX + id;
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(infoKey))) {
            stringRedisTemplate.opsForHash().put(infoKey, "status", String.valueOf(status));
        } else if (voucher.getType() == 1) {
            // 如果是秒杀券但缓存不存在，重新预热
            cacheVoucherInfo(voucher);
        }
        
        log.info("优惠券状态更新：id={}, status={}", id, status);
    }
    
    @Override
    public List<Voucher> listByShop(Long shopId) {
        LambdaQueryWrapper<Voucher> wrapper = new LambdaQueryWrapper<Voucher>()
                .eq(Voucher::getShopId, shopId)
                .eq(Voucher::getStatus, 1) // 只查上架的
                .gt(Voucher::getStock, 0) // 库存大于0
                .orderByAsc(Voucher::getType) // 普通券在前，秒杀券在后
                .orderByAsc(Voucher::getBeginTime); // 按开始时间排序
        
        List<Voucher> list = this.list(wrapper);
        
        // 自动更新过期状态
        updateExpiredVouchers(list);
        
        // 再次过滤，移除已过期的
        LocalDateTime now = LocalDateTime.now();
        return list.stream()
                .filter(v -> v.getEndTime() == null || v.getEndTime().isAfter(now))
                .toList();
    }
    
    @Override
    public Voucher getByIdForUser(Long id) {
        Voucher voucher = this.getById(id);
        if (voucher == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "优惠券不存在");
        }
        
        // 检查是否上架
        if (voucher.getStatus() != 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "优惠券已下架");
        }
        
        // 检查是否过期
        if (voucher.getEndTime() != null && voucher.getEndTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "优惠券已过期");
        }
        
        return voucher;
    }
    
    @Override
    public Long seckillVoucher(Long voucherId) {
        // 1. 获取当前用户
        Long userId = UserHolder.getUser();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "请先登录");
        }
        
        // 2. ⭐ 从 Redis 缓存读取秒杀券信息（使用 hmget 精确获取字段，比 entries 更高效）
        String infoKey = SECKILL_INFO_PREFIX + voucherId;
        List<Object> fields = stringRedisTemplate.opsForHash().multiGet(
            infoKey, 
            List.of("type", "status", "beginTime", "endTime")
        );
        
        // 缓存未命中，回退到数据库
        if (fields == null || fields.get(0) == null) {
            Voucher voucher = this.getById(voucherId);
            if (voucher == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND, "优惠券不存在");
            }
            cacheVoucherInfo(voucher);
            fields = stringRedisTemplate.opsForHash().multiGet(
                infoKey, 
                List.of("type", "status", "beginTime", "endTime")
            );
        }
        
        // 3. 校验优惠券类型和状态
        int type = Integer.parseInt(fields.get(0).toString());
        int status = Integer.parseInt(fields.get(1).toString());
        
        if (type != 1) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "该优惠券不是秒杀券");
        }
        if (status != 1) {
            throw new BusinessException(ErrorCode.CONFLICT, "优惠券已下架");
        }
        
        // 4. 校验秒杀时间
        LocalDateTime now = LocalDateTime.now();
        Object beginTimeObj = fields.get(2);
        Object endTimeObj = fields.get(3);
        
        if (beginTimeObj != null && !beginTimeObj.toString().isEmpty()) {
            LocalDateTime beginTime = LocalDateTime.parse(beginTimeObj.toString(), DT_FORMATTER);
            if (now.isBefore(beginTime)) {
                throw new BusinessException(ErrorCode.CONFLICT, "秒杀还未开始");
            }
        }
        if (endTimeObj != null && !endTimeObj.toString().isEmpty()) {
            LocalDateTime endTime = LocalDateTime.parse(endTimeObj.toString(), DT_FORMATTER);
            if (now.isAfter(endTime)) {
                throw new BusinessException(ErrorCode.CONFLICT, "秒杀已结束");
            }
        }
        
        // 5. 执行 Lua 脚本（原子操作：判库存、判重复、扣库存、发 Stream 消息）
        Long result = stringRedisTemplate.execute(
                SECKILL_SCRIPT,
                List.of(
                        "seckill:stock:" + voucherId,
                        "seckill:order:" + voucherId
                ),
                STREAM_KEY,
                voucherId.toString(),
                userId.toString()
        );
        
        // 6. 判断结果
        if (result == null) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "系统错误");
        }
        int r = result.intValue();
        if (r == 1) {
            throw new BusinessException(ErrorCode.CONFLICT, "库存不足");
        }
        if (r == 2) {
            throw new BusinessException(ErrorCode.CONFLICT, "不能重复购买");
        }
        
        // 7. 返回（异步下单，先返回 null 表示"排队中"）
        log.info("秒杀成功：userId={}, voucherId={}, 已进入排队", userId, voucherId);
        return null;
    }
    
    /**
     * 验证优惠券参数
     */
    private void validateVoucher(Voucher voucher) {
        // 基础验证
        if (voucher.getShopId() == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "商铺ID不能为空");
        }
        if (voucher.getTitle() == null || voucher.getTitle().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "标题不能为空");
        }
        if (voucher.getPayValue() == null || voucher.getPayValue() <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "实付金额必须大于0");
        }
        if (voucher.getActualValue() == null || voucher.getActualValue() <= 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "抵扣金额必须大于0");
        }
        if (voucher.getActualValue() <= voucher.getPayValue()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "抵扣金额必须大于实付金额");
        }
        if (voucher.getStock() == null || voucher.getStock() < 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "库存不能为负数");
        }
        
        // 秒杀券特殊验证
        if (voucher.getType() == 1) {
            if (voucher.getBeginTime() == null) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "秒杀券必须设置开始时间");
            }
            if (voucher.getEndTime() == null) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "秒杀券必须设置结束时间");
            }
            if (!voucher.getEndTime().isAfter(voucher.getBeginTime())) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "结束时间必须晚于开始时间");
            }
        }
    }
    
    /**
     * 自动更新过期优惠券状态
     */
    private void updateExpiredVouchers(List<Voucher> vouchers) {
        LocalDateTime now = LocalDateTime.now();
        for (Voucher voucher : vouchers) {
            // 如果是上架状态且已过期，自动更新为过期状态
            if (voucher.getStatus() == 1 && voucher.getEndTime() != null 
                    && voucher.getEndTime().isBefore(now)) {
                voucher.setStatus(3); // 设置为过期
                this.updateById(voucher);
                log.info("优惠券自动过期：id={}", voucher.getId());
            }
        }
    }
}
