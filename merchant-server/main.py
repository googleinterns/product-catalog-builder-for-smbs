from flask import Flask, request
import utils
import fcm
import traceback
import time

app = Flask(__name__)


@app.route('/')
def home(methods=['GET']):
    return "Welcome to merchant side back-end server"


@app.route('/notify/merchant/<mid>')
def notify_merchant(mid, methods=['GET']):
    merchant = utils.get_merchant(mid)
    if merchant == None:
        return "Invalid merchant ID", 400
    fcm.notify(merchant["token"])
    return "OK"


@app.route('/order/merchant/<mid>', methods=['POST'])
def start_order(mid):
    '''
    Create a new order and notify merchant "mid"
    '''
    merchant = utils.get_merchant(mid)
    if merchant == None:
        return "Invalid merchant ID", 400
    data = request.get_json()
    # Add timestamp if doesn't exists
    millis = int(round(time.time() * 1000))
    data["timestamp"] = data.get("timestamp", millis)
    products = utils.get_products_for_merchant(mid, data["items"])
    data["items"] = products
    try:
        fcm.notify_place_order(merchant["token"], data)
        utils.save_order(mid, data)
    except Exception as e:
        print(traceback.format_exc())
        return "Internal server error", 500
    return 'OK'


@app.route('/order/confirm/merchant/<mid>', methods=['POST'])
def confirm_order(mid):
    '''
    Notify merchant of order confirmation from customer
    '''
    merchant = utils.get_merchant(mid)
    if merchant == None:
        return "Invalid merchant ID", 400
    data = request.get_json()
    if data.get("oid") == None:
        return "Order ID not provided", 400
    oid = data["oid"]
    order = utils.confirm_order(mid, oid)
    if order == None:
        return "Invalid order ID", 400
    fcm.notify_confirm_order(merchant["token"], order)
    return "OK"


@app.route('/products/page', methods=['GET'])
def get_page():
    '''
    Params to be defined as query string:
    mid: merchat ID of the merchant to fetch products from
    page_num: page number to fetch
    Returns products from merchant's ('mid') inventory for page 'page_num' 
    '''
    try:
        mid = request.args.get('mid')
        page_num = int(request.args.get('page_num'))
        if page_num < 1:
            return "Page number must be a positive integer", 400
    except Exception as e:
        return "Invalid request parameters", 400
    products = utils.get_products_in_page(mid, page_num)
    return {"products": products}

@app.route('/inventory', methods=['GET'])
def get():
    '''
    Get all products in the merchant inventory
    '''
    try:
        mid = request.args.get('mid')
    except Exception as e:
        return 'Missing mid', 400
    merchant = utils.get_merchant(mid)
    if merchant == None:
        return "Invalid mid", 400
    products = utils.get_inventory(mid)
    return {
        "merchant_name": merchant["name"],
        "products": products
    }

if __name__ == "__main__":
    app.run(host="127.0.0.1", port=8080, debug=True)
