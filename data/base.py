from abc import ABC, abstractmethod
from typing import List, Dict

class IAPIGetter(ABC):
    """
    Interface for retrieving coin prices from APIs.
    """
    def __init__(self, url: str, default_currency: str = None):
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

