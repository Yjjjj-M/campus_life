import request from './request'

// 关注用户
export function followUser(followUserId: number) {
  return request.post(`/user/follow/${followUserId}`)
}

// 取消关注
export function unfollowUser(followUserId: number) {
  return request.delete(`/user/follow/${followUserId}`)
}

// 查询我关注的人
export function getFollowing() {
  return request.get('/user/follow/following')
}

// 查询我的粉丝
export function getFollowers() {
  return request.get('/user/follow/followers')
}

// 查询是否关注了某用户
export function isFollowing(followUserId: number) {
  return request.get(`/user/follow/isFollowing/${followUserId}`)
}

// 查询我的 Feed 流
export function getMyFeed(cursor?: number, size: number = 10) {
  return request.get('/user/feed', {
    params: { cursor, size }
  })
}
