from abc import ABC, abstractmethod
from typing import List, Dict

class IAPIGetter(ABC):
    """
    Interface for retrieving coin prices from APIs.
    """
    def __init__(self, url: str):
        self.api_url = url

    @abstractmethod
    def get_latest_price(self, coin: str) -> float:
        pass

    @abstractmethod
    def get_interval_price(self, coin: str) -> List[float]:
        pass

    @abstractmethod
    def get_latest_prices(self, coin: List[str]) -> Dict[str, float]:
        pass

    @abstractmethod
    def get_interval_prices(self, coin: List[str]) -> Dict[str, List[float]]:
        pass

