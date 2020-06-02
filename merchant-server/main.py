from flask import Flask, request
import utils
import fcm
import traceback

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
    merchant = utils.get_merchant(mid)
    if merchant == None:
        return "Invalid merchant ID", 400
    data = request.get_json()
    products = utils.get_products_for_merchant(mid, data["items"])
    data["items"] = products
    try:
        fcm.notify_place_order(merchant["token"], data)
    except Exception as e:
        print(traceback.format_exc())
        return "Internal server error", 500
    return 'OK'

if __name__=="__main__":
    app.run(host="127.0.0.1", port=8080, debug=True)
