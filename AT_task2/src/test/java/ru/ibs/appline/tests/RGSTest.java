package ru.ibs.appline.tests;

import org.hamcrest.junit.MatcherAssert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;

public class RGSTest {
    private static WebDriver driver;
    private static WebDriverWait wait;


    @BeforeEach
    public void before() {
        System.setProperty("webdriver.chrome.driver", "src/test/resources/webdriver/chromedriver.exe");
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);

        wait = new WebDriverWait(driver, 10, 1000);

        String baseUrl = "https://www.rgs.ru/";
        driver.get(baseUrl);
    }

    private static Stream<Arguments> names() {
        return Stream.of(
                Arguments.of("Иванов", "Александр","Сергеевич"),
                Arguments.of("Петров", "Иван","Андреевич"),
                Arguments.of("Сидоров", "Тимур","Александрович")
        );
    }

    @ParameterizedTest
    @MethodSource("names")
    public void rgsTest(String lastName, String firstName, String middleName) {

        //Закрыть рекламу
        closeAdv(By.xpath("//button[@class='CloseButton']"), By.xpath("//iframe[@class ='flocktory-widget']"));

        //Закрыть cookies
        closeAdv(By.xpath("//div[@class='btn btn-default text-uppercase']"), null);

        //Нажать кнопку "Меню"
        String menuButtonXPath = "//*[@class = 'navbar-collapse collapse']//a[contains(text(), 'Меню')]";
        WebElement menuButton = driver.findElement(By.xpath(menuButtonXPath));
        menuButton.click();

        //Нажать кнопку "Компаниям"
        WebElement companiesButton = menuButton.findElement(By.xpath("./..//a[contains(text(), 'Компаниям')]"));
        companiesButton.click();

        //Закрыть уведомления
        closeAdv(By.xpath("//div[@class = 'PushTip']//div[@class = 'PushTip-close']"), By.xpath("//iframe[@class ='flocktory-widget']"));

        //Нажать кнопку "Страхование здоровья"
        String healthInsuranceXPath = "//div[@class = 'col-rgs-content-center-col']//*[contains(text(), 'Страхование здоровья')]";
        WebElement healthInsuranceButton = driver.findElement(By.xpath(healthInsuranceXPath));
        healthInsuranceButton.click();

        //Переход на открывшуюся страницу
        switchToTabByText("ДМС для сотрудников - добровольное медицинское страхование от Росгосстраха");

        //Нажать кнопку "Добровольное медицинское страхование"
        String voluntaryInsuranceXPath = "//div[@class='list-group list-group-rgs-menu']//a[contains(text(), 'Добровольное медицинское страхование')]";
        WebElement voluntaryInsuranceButton = driver.findElement(By.xpath(voluntaryInsuranceXPath));
        voluntaryInsuranceButton.click();

        //Проверить, что заголовок - "Добровольное медицинское страхование"
        MatcherAssert.assertThat("Заголовок не совпадает", driver.findElement(By.xpath("//h1")).getText(),is("Добровольное медицинское страхование"));

        //Закрыть уведомления
        closeAdv(By.xpath("//div[@class = 'PushTip']//div[@class = 'PushTip-close']"), By.xpath("//iframe[@class ='flocktory-widget']"));

        //Закрыть рекламу
        closeAdv(By.xpath("//div[@class='widget__close widget__close--ribbon']"), By.xpath("//iframe[@class ='flocktory-widget']"));

        //Нажать на кнопку "Отправить заявку"
        String sendRequestXPath = "//a[contains(text(), 'Отправить заявку')]";
        WebElement sendRequestButton = driver.findElement(By.xpath(sendRequestXPath));
        sendRequestButton.click();

        //Проверить, что присутствует текст "Заявка на добровольное медицинское страхование"
        Assertions.assertTrue(driver.getPageSource().contains("Заявка на добровольное медицинское страхование"));

        //Заполнить поля данными
        String fieldXPath = "//input[@name='%s']";
        fillInputField(driver.findElement(By.xpath(String.format(fieldXPath, "LastName"))), lastName);
        fillInputField(driver.findElement(By.xpath(String.format(fieldXPath, "FirstName"))), firstName);
        fillInputField(driver.findElement(By.xpath(String.format(fieldXPath, "MiddleName"))), middleName);

        WebElement selectElem = driver.findElement(By.xpath("//select"));
        Select select = new Select(selectElem);
        select.selectByVisibleText("Москва");

        fillInputField(driver.findElement(By.xpath("//input[contains(@data-bind,'Phone')]")), "(999) 999-99-99");
        fillInputField(driver.findElement(By.xpath("//input[contains(@data-bind,'ContactDate')]")), "01.05.2022");
        fillInputField(driver.findElement(By.xpath(String.format(fieldXPath, "Email"))), "qwertyqwerty");
        fillInputField(driver.findElement(By.xpath("//textarea[@name='Comment']")), "Комментарий");

        //Поставить галочку на обработку данных
        String checkboxXPath = "//input[@class='checkbox']";
        WebElement checkbox = driver.findElement(By.xpath(checkboxXPath));
        checkbox.click();

        //Нажать кнопку отправить
        String sendXPath = "//button[contains(text(), 'Отправить')]";
        WebElement sendButton = driver.findElement(By.xpath(sendXPath));
        sendButton.click();

        //Проверить, что у поля почты высветилась ошибка ввода
        String errorEmailXPath = "//span[@class='validation-error-text'][contains(text(), 'Введите адрес электронной почты')]";
        Assertions.assertTrue(elementExist(By.xpath(errorEmailXPath)), "Сообщение об ошибке почты отсутсвует или изменилось");
    }

    @AfterEach
    public void after() {
        driver.quit();
    }

    /**
     * Закрытие всплывающих окон
     *
     * @param by         - параметр для поиска элемента
     * @param frameXPath - параметр по которому идет переключение на frame
     */
    public void closeAdv(By by, By frameXPath) {
        //Сбрасываем ожидание до 2 секунд, чтобы не ждать долго
        driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
        //Пробуем кликнуть на элемент
        try {
            if (frameXPath != null)
                driver.switchTo().frame(driver.findElement(frameXPath));
            driver.findElement(by);
            driver.findElement(by).click();
        }
        //Если не находим элемент, то обрабатываем исключение
        catch (NoSuchElementException ignore) {
            //Возвращаем ожидание в изначальные 10 секунд
        } finally {
            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
            if (frameXPath != null)
                driver.switchTo().parentFrame();
        }

    }

    /**
     * Переключение на другую вкладку по тексту
     *
     * @param text - текст вкладки для переключения
     */
    public void switchToTabByText(String text) {
        String myTab = driver.getWindowHandle();
        ArrayList<String> newTab = new ArrayList<>(driver.getWindowHandles());
        for (String s : newTab) {
            if (!s.equals(myTab)) {
                driver.switchTo().window(s);
                if (driver.getTitle().contains(text))
                    return;
            }
        }
        Assertions.fail("Вкладка " + text + " не найдена");
    }

    /**
     * Заполнение полей определённым значений
     *
     * @param element - веб элемент (поле какое-то) которое планируем заполнить)
     * @param value   - значение которы мы заполняем веб элемент (поле какое-то)
     */
    private void fillInputField(WebElement element, String value) {
        element.click();
        element.sendKeys(value);
        boolean checkFlag = wait.until(ExpectedConditions.attributeContains(element, "value", value));
        Assertions.assertTrue(checkFlag, "Поле было заполнено некорректно");
    }

    /**
     * Проверка существования элемента на странице
     *
     * @param by - параметр для поиска элемента
     * @return - возвращиет true если элемент найден и false в остальных случаях
     */
    private boolean elementExist(By by) {
        try {
            driver.findElement(by);
        } catch (NoSuchElementException ignore) {
            return false;
        }
        return true;

    }
}
