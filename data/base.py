from abc import ABC, abstractmethod
from typing import List, Dict

class BitPredictException(Exception):
    pass

class IAPIGetter(ABC):
    """
    Interface for retrieving coin prices from APIs.
    """
    def __init__(self, api_key: str, url: str, default_currency: str = None):
        self.api_key = api_key
        self.api_url = url
        self.default_currency = default_currency

    @abstractmethod
    def get_latest_price(self, coin: str, currency: str = None) -> float:
        pass

    @abstractmethod
    def get_interval_price(self, coin: str, currency: str = None) -> List[float]:
        pass

    @abstractmethod
    def get_latest_prices(self, coin: List[str], currency: str = None) -> Dict[str, float]:
        pass

    @abstractmethod
    def get_interval_prices(self, coin: List[str], currency: str = None) -> Dict[str, List[float]]:
        pass

class TimeInterval:
    def __init__(self):
        raise NotImplemented()

class APIGetterExceptions:
    class NoCurrency(BitPredictBaseException):
        pass

    class InvalidRequest(BitPredictBaseException):
        def __init__(self, coin: str, currency: str):
            super(coin, currency)
