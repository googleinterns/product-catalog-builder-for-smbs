class BaseHttpException(Exception):

    def __init__(self, message):
        Exception.__init__(self)
        self.message = message

    def to_dict(self):
        response = {}
        response["message"] = self.message
        return response


class InvalidRequest(BaseHttpException):
    status_code = 400

    def __init__(self, message):
        BaseHttpException.__init__(self, message)


class UnprocessableRequest(BaseHttpException):
    status_code = 422

    def __init__(self, message):
        BaseHttpException.__init__(self, message)
