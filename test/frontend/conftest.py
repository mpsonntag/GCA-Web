import pytest
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.common.action_chains import ActionChains
import Cookies


# Firefox-workaround, as move_to_element not working if element not in current screen
def scroll(driver, object):
    if 'firefox' in driver.capabilities['browserName']:
        scroll_by_coord = 'window.scrollTo(%s,%s);' % (
            object.location['x'],
            object.location['y']
        )
        driver.execute_script(scroll_by_coord)


def move_to_element_by_class_name(driver, name):
    element = driver.find_element_by_class_name(name)
    scroll(driver, element)
    hover = ActionChains(driver).move_to_element(element)
    hover.perform()


def maximize_login(request):
    if request.param == "chrome":
        driver = webdriver.Remote(
            command_executor="http://" + Cookies.get_host_ip() + ":4444/wd/hub",
            desired_capabilities={'browserName': 'chrome', 'javascriptEnabled': True}
        )
    if request.param == "firefox":
        driver = webdriver.Remote(
            command_executor="http://" + Cookies.get_host_ip() + ":4444/wd/hub",
            desired_capabilities={'browserName': 'firefox', 'javascriptEnabled': True}
        )

    session = request.node
    for item in session.items:
        cls = item.getparent(pytest.Class)
        setattr(cls.obj, "driver", driver)

    driver.get("http://" + Cookies.get_host_ip() + ":9000/login")
    driver.maximize_window()

    return driver


@pytest.fixture(params=["chrome", "firefox"], scope="session")
def setup_login(request):

    driver = maximize_login(request)
    yield
    driver.quit()


@pytest.fixture(params=["chrome", "firefox"], scope="session")
def setup_editor(request):

    driver = maximize_login(request)
    Cookies.load_cookies(driver)
    driver.get("http://" + Cookies.get_host_ip() + ":9000/conference/BC14/submission")
    WebDriverWait(driver, 30).until(
        EC.presence_of_element_located((By.CLASS_NAME, 'btn-success'))
    )
    yield
    driver.quit()
