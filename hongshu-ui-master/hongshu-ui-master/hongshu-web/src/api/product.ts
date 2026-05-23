import request from '@/utils/request';

// 获取商品详情
export function getProductById(id: string | number) {
    return request({
        url: '/web/product/get/' + id,
        method: 'get'
    });
}

// 购买商品：quantity 通过 params 传递，拼接在 URL 后
export function buyProduct(productId: string | number, quantity: number) {
    return request({
        url: `/web/product/buy/${productId}`,
        method: 'post',
        params: {
            quantity: quantity
        }
    });
}
