from .base import IAPIGetter
from requests import Session
from typing import List, Dict
import json

class CMCAPI(IAPIGetter):
    HEADERS = {
            'Accepts' : 'application/json', 
            'X-CMC_PRO_API_KEY': self.__api_key
    }

    def get_latest_price(self, coin: str, currency: str = None) -> float:
        url_extra = "quotes/latest"
        if(currency is None):
            if(self.default_currency is None) raise IAPIGetter.NoCurrency()
            else:
                currency = self.default_currency
        parameters = {
          'convert' : currency.upper(),
          'symbol' : coin.upper(),
        }

        session = Session()
        session.headers.update(headers)

        try:
            response = session.get(url, params=parameters)
            data = json.loads(response.text)
            try:
                return data['data'][coin_symbol.upper()]['quote'][currency_symbol.upper()]['price']
            except KeyError:
                print(f"Request Error: \n{data}")
                raise IAPIGetter.InvalidRequest(coin, currency)
        except (Exchange.ConnectionError, Exchange.TooManyRedirects, Exchange.Timeout) as e:
            print(e)
            
            raise e

    def get_interval_price(self, coin: str, currency: str = None) -> List[float]:
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

