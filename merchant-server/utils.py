
import firebase_admin
from firebase_admin import credentials, messaging, firestore

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
        return None

def get_products_for_merchant(mid, items):
    '''
    Get products from list of EANs for given merchant
    '''
    order_products = {}
    for item in items:
        order_products[item["EAN"]] = item
    query = db.collection(f"merchants/{mid}/products").where("EAN", "in", list(order_products.keys())).stream()
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
        query = db.collection("products").where("EAN", "in", remaining_products).stream()
        for doc in query:
            product = doc.to_dict()
            product["discounted_price"] = product["MRP"]
            product["quantity"] = order_products[product["EAN"]]["quantity"]
            products.append(product)
    return products
