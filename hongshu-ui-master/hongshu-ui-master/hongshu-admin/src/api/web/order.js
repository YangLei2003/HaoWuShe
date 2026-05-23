import request from '@/utils/request'

export function listOrder(query) {
    return request({
        url: '/web/order/list',
        method: 'get',
        params: query
    })
}

export function delOrder(id) {
    return request({
        url: '/web/order/' + id,
        method: 'delete'
    })
}

// 修改订单
export function updateOrder(data) {
    return request({
        url: '/web/order/update', // 这里的 URL 必须对应你后端 Controller 的路径
        method: 'put',     // 通常修改用 put
        data: data
    })
}