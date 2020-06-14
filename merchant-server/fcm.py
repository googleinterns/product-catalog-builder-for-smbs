from pyfcm import FCMNotification
from keys import FCM_API_KEY
from constants import NEW_ORDER, ONGOING

push_service = FCMNotification(api_key=FCM_API_KEY)


def notify(token):
    message_title = "Update Inventory"
    message_body = "More than 37% of merchants have updated their inventory in last 5 days"
    result = push_service.notify_single_device(
        registration_id=token, message_title=message_title, message_body=message_body)
    return result


def notify_place_order(token, data):
    '''
    Notify merchant with the new order
    '''
    data["status"] = NEW_ORDER
    result = push_service.notify_single_device(
        registration_id=token,
        data_message=data
    )
    return result


def notify_confirm_order(token, data):
    '''
    Notify order confirmation to merchant
    '''
    data["status"] = ONGOING
    result = push_service.notify_single_device(
        registration_id=token,
        data_message=data
    )
    return result
