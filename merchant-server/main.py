from flask import Flask, request, jsonify
from flask_cors import CORS
import utils
import fcm
import traceback
import time
from exceptions import BaseHttpException, InvalidRequest

app = Flask(__name__)
CORS(app)

@app.route('/')
def home(methods=['GET']):
    return {"message": "Welcome to merchant side back-end server"}


@app.route('/notify/merchant/<mid>')
def notify_merchant(mid, methods=['GET']):
    merchant = utils.get_merchant(mid)
    fcm.notify(utils.get_token(merchant))
    return {"message": "OK"}


@app.route('/order/merchant/<mid>', methods=['POST'])
def start_order(mid):
    '''
    Create a new order and notify merchant "mid"
    '''
    merchant = utils.get_merchant(mid)
    data = request.get_json()
    # Add timestamp if doesn't exists
    millis = int(round(time.time() * 1000))
    data["timestamp"] = data.get("timestamp", millis)
    products = utils.get_products_for_merchant(mid, data["items"])
    data["items"] = products
    fcm.notify_place_order(utils.get_token(merchant), data)
    utils.save_order(mid, data)
    return {"message": "OK"}


@app.route('/order/confirm/merchant/<mid>', methods=['POST'])
def confirm_order(mid):
    '''
    Notify merchant of order confirmation from customer
    '''
    merchant = utils.get_merchant(mid)
    data = request.get_json()
    oid = data["oid"]
    order = utils.confirm_order(mid, oid)
    fcm.notify_confirm_order(utils.get_token(merchant), order)
    return {"message": "OK"}


@app.route('/products/page', methods=['GET'])
def get_page():
    '''
    Params to be defined as query string:
    mid: merchat ID of the merchant to fetch products from
    last_ean: EAN of last product in previous page (omit paramter incase of first page query)
    Returns PRODUCT_PER_PAGE number of products from merchant's ('mid') inventory having EAN after 'last_ean' 
    '''
    mid = request.args.get("mid")
    if mid == None:
        raise KeyError("mid")
    last_ean = request.args.get("last_ean")
    # If last_ean is not provided then request defaults to first page
    if last_ean == None:
        last_ean = "0"
    products = utils.get_products_in_page(mid, last_ean)
    return {"products": products, "message": "OK"}


@app.route('/inventory', methods=['GET'])
def get_inventory():
    '''
    Get all products in the merchant inventory
    '''
    try:
        mid = request.args.get("mid")
    except Exception as e:
        raise InvalidRequest("Missing mid")
    merchant = utils.get_merchant(mid)
    products = utils.get_inventory(mid)
    return {
        "merchant_name": merchant["name"],
        "products": products,
        "message": "OK"
    }

@app.route('/merchants/all', methods=['GET'])
def get_all_merchants():
    '''
    Get information for all merchants
    '''
    merchants = utils.get_all_merchants()
    return {
        "message": "OK",
        "merchants": merchants
    }

@app.errorhandler(BaseHttpException)
def handle_http_exception(error):
    response = jsonify(error.to_dict())
    response.status_code = error.status_code
    return response


@app.errorhandler(KeyError)
def handle_missing_parameters(error):
    response = jsonify({"message": f"Missing field {str(error)}"})
    response.status_code = 400
    return response


if __name__ == "__main__":
    app.run(host="127.0.0.1", port=8080, debug=True)
