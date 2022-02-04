from base import IAPIGetter, APIGetterExceptions, TimeInterval
from requests import Session
from requests.exceptions import ConnectionError, TooManyRedirects, Timeout
from typing import List, Dict
import json

class CMCAPI(IAPIGetter):
    def __init__(self, api_key: str, url: str, default_currency: str = None):
        super().__init__(api_key, url, default_currency)
        self.headers = {
            'Accepts': 'applicaiton/json',
            'X-CMC_PRO_API_KEY': self.api_key
        }

    def get_currency(self, currency: str) -> str:
        """ Returns default_currency if currency is None """
        if(currency is None):
            if(self.default_currency is None): raise APIGetterExceptions.NoCurrency()
            else:
                return self.default_currency
        return currency

    def get_latest_price(self, coin: str, currency: str = None) -> float:
        currency = self.get_currency(currency)

        url_extra = "quotes/latest"
        parameters = {
          'convert' : currency.upper(),
          'symbol' : coin.upper(),
        }

        session = Session()
        session.headers.update(self.headers)

        try:
            response = session.get(self.api_url + url_extra, params=parameters)
            data = json.loads(response.text)
            
            try:
                return data['data'][coin.upper()]['quote'][currency.upper()]['price']
            except KeyError:
                raise APIGetterExceptions.InvalidRequest(coin, currency)
        except (ConnectionError, TooManyRedirects, Timeout) as e:
            print("Error in connecting to the API. Perhaps the API is down?") 
            raise e

    def get_interval_price(self, time_interval: TimeInterval, coin: str, currency: str = None) -> List[float]:
        pass

    def get_latest_prices(self, coins: List[str], currency: str = None) -> Dict[str, float]:
        d = dict()
        for c in coins:
            d[c.upper()] = self.get_latest_price(c.upper(), currency)
        return d

    def get_interval_prices(self, coins: List[str], currency: str = None) -> Dict[str, List[float]]:
        d = dict()
        for c in coins:
            d[c.upper()] = self.get_interval_price(c.upper(), currency)
        return d

