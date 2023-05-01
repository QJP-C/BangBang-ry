import request from '@/utils/request'

// 查询帖子审核列表
export function listPost(query) {
  return request({
    url: '/bang/postAudit/list',
    method: 'get',
    params: query
  })
}

// 查询帖子审核详细
export function getPost(id) {
  return request({
    url: '/bang/postAudit/' + id,
    method: 'get'
  })
}

// 新增帖子审核
export function addPost(data) {
  return request({
    url: '/bang/postAudit',
    method: 'post',
    data: data
  })
}

// 修改帖子审核
export function updatePost(data) {
  return request({
    url: '/bang/postAudit',
    method: 'put',
    data: data
  })
}

// 删除帖子审核
export function delPost(id) {
  return request({
    url: '/bang/postAudit/' + id,
    method: 'delete'
  })
}
