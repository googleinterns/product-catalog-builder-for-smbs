from flask import Flask, request
import utils
import fcm
import traceback
import time

app = Flask(__name__)

@app.route('/')
def home():
    return "Welcome to merchant side back-end server"

@app.route('/notify/merchant/<mid>')
def notify_merchant(mid):
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

if __name__=="__main__":
    app.run(host="127.0.0.1", port=8080, debug=True)
