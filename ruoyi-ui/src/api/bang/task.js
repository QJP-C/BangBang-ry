import request from '@/utils/request'

// 查询任务审核列表
export function listTask(query) {
  return request({
    url: '/bang/taskAudit/list',
    method: 'get',
    params: query
  })
}

// 查询任务审核详细
export function getTask(id) {
  return request({
    url: '/bang/taskAudit/' + id,
    method: 'get'
  })
}

// 新增任务审核
export function addTask(data) {
  return request({
    url: '/bang/taskAudit',
    method: 'post',
    data: data
  })
}

// 修改任务审核
export function updateTask(data) {
  return request({
    url: '/bang/taskAudit',
    method: 'put',
    data: data
  })
}

// 删除任务审核
export function delTask(id) {
  return request({
    url: '/bang/taskAudit/' + id,
    method: 'delete'
  })
}
