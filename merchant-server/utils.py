import firebase_admin
from firebase_admin import credentials, messaging, firestore
import datetime
from constants import ONGOING, PRODUCTS_PER_PAGE
from exceptions import InvalidRequest, UnprocessableRequest

cred = credentials.Certificate("key.json")
app = firebase_admin.initialize_app(cred)
db = firestore.client()


def get_merchant(mid):
    '''
    Retrive merchant document from the firestore
    '''
    doc_ref = db.collection("merchants").document(mid)
    doc = doc_ref.get()
    if doc.exists:
        return doc.to_dict()
    else:
        raise InvalidRequest("Invalid merchant ID")


def get_token(merchant):
    if merchant.get("token") == None:
        raise InvalidRequest("Merchant not available")
    return merchant["token"]


def get_products_for_merchant(mid, items):
    '''
    Get products from list of EANs for given merchant
    '''
    order_products = {}
    for item in items:
        order_products[item["EAN"]] = item
    query = db.collection(f"merchants/{mid}/products") \
        .where("EAN", "in", list(order_products.keys())) \
        .stream()
    products = []
    for doc in query:
        product = doc.to_dict()
        ean = product["EAN"]
        product["quantity"] = order_products[ean]["quantity"]
        products.append(product)
        # remove products available with merchant
        order_products.pop(ean)

    remaining_products = list(order_products.keys())
    if len(remaining_products) > 0:
        # products that are not in merchant's inventory
        query = db.collection("products").where(
            "EAN", "in", remaining_products).stream()
        for doc in query:
            product = doc.to_dict()
            product["discounted_price"] = product["MRP"]
            product["quantity"] = order_products[product["EAN"]]["quantity"]
            products.append(product)
    return products


def save_order(mid, order):
    '''
    Save given order to database for the associated merchant
    '''
    oid = order["oid"]
    db.collection(f"merchants/{mid}/orders").document(oid).set(order)


def confirm_order(mid, oid):
    '''
    Confirm order 'oid' to merchant 'mid'
    '''
    print(oid)
    order_ref = db.collection(f"merchants/{mid}/orders").document(oid)
    order = order_ref.get()
    if order.exists:
        data = order.to_dict()
        data["status"] = ONGOING
        order_ref.set(data)
        return data
    else:
        raise InvalidRequest("Invalid order ID")


def get_products_in_page(mid, page_num):
    '''
    Returns a list of products from the inventory of the given merchant
    for the corresponding page number of paginated view
    '''
    merchant = get_merchant(mid)
    num_products = merchant["num_products"]
    # When page query page number is more than total number of pages
    if (page_num - 1) * PRODUCTS_PER_PAGE >= num_products:
        return []
    prev_page_last_sno = (page_num - 1) * PRODUCTS_PER_PAGE
    query = db.collection(f"merchants/{mid}/products") \
        .order_by("sno") \
        .start_after({
            "sno": prev_page_last_sno
        }) \
        .limit(PRODUCTS_PER_PAGE) \
        .stream()
    products = []
    for doc in query:
        product = doc.to_dict()
        products.append(product)
    return products


def get_inventory(mid):
    '''
    Get all products in merchant's inventory
    '''
    products_ref = db.collection(f"merchants/{mid}/products").stream()
    products = []
    for doc in products_ref:
        product = doc.to_dict()
        products.append(product)
    return products
