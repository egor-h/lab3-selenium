import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.stream.IntStream;

public class Main {
    public static final String GROUP = "РИЗ-470028у";

    public static void main(String[] args) {

        ChromeOptions chromeOptions = new ChromeOptions()
                .addArguments("--window-size=1280,800");

        WebDriver webDriver = new ChromeDriver(chromeOptions);
        try {
            new Main().printSched(webDriver);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            webDriver.close();
            webDriver.quit();
        }
    }

    String findSecondWindow(WebDriver driver, String firstWindow) {
        return driver.getWindowHandles().stream()
                .filter(wid -> !wid.equals(firstWindow))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Second window not found"));
    }

    void printSched(WebDriver driver) throws Exception {
        WebDriverWait wait = new WebDriverWait(driver, 10);
        driver.get("https://google.ru");

        String currentWindow = driver.getWindowHandle();

        WebElement queryField = driver.findElement(By.name("q"));
        queryField.sendKeys("Урфу" + Keys.ENTER);

        List<WebElement> results = driver.findElements(By.cssSelector(".rc a"));
        results.get(0).click();

        wait.until(ExpectedConditions.numberOfWindowsToBe(2));
        String newWindow = findSecondWindow(driver, currentWindow);
        driver.switchTo().window(newWindow);

        // "Студентам"
        new WebDriverWait(driver, 10).until(ExpectedConditions.elementToBeClickable(By.cssSelector(".main-menu-item > a"))).click();
        wait.withTimeout(Duration.ofSeconds(3));

        // "Расписание занятий"
        new WebDriverWait(driver, 10).until(ExpectedConditions.elementToBeClickable(By.cssSelector(".nav-list > p > a"))).click();

        // Поиск по группе
        WebElement grpQuery = new WebDriverWait(driver, 10).until(ExpectedConditions.elementToBeClickable(By.cssSelector(".shedule-search-input > input")));
        grpQuery.click();
        grpQuery.sendKeys(GROUP);

        new WebDriverWait(driver, 10).until(ExpectedConditions.elementToBeClickable(By.cssSelector(".shedule-group-table")));
        String today = driver.findElement(By.cssSelector(".shedule-group-table tr.divide")).getText();
        String weekday = driver.findElement(By.cssSelector(".shedule-weekday-name")).getText();

        List<WebElement> times = driver.findElements(By.cssSelector(".shedule-weekday-time"));
        List<WebElement> subjects = driver.findElements(By.cssSelector("dl.shedule-weekday-item > dd"));
        List<WebElement> type = driver.findElements(By.cssSelector("dl.shedule-weekday-item > dt > span:first-child"));
        List<WebElement> lecturer = driver.findElements(By.cssSelector("dl.shedule-weekday-item > dt > span:last-child"));

        List<WebElement> schedElements = driver.findElements(By.cssSelector(".shedule-group-table tr"));
        int todaysLecturesCount = countTodaysLecturers(schedElements);

        System.out.println(String.format("%s %s", weekday, today));
        IntStream.range(0, todaysLecturesCount).forEach(i -> {
            String spaces = multiplySequence(" ", times.get(i).getText().length()+2);
            System.out.println(String.format("%s - %s\n%s%s\n%s%s",
                    times.get(i).getText(), subjects.get(i).getText(),
                    spaces, type.get(i).getText(),
                    spaces, lecturer.get(i).getText()));
        });
    }

    String multiplySequence(String seq, int times) {
        return new String(new char[times]).replace("\0", seq);
    }

    int countTodaysLecturers(List<WebElement> schedElements) {
        // Все элементы .shedule-weekday-row между первыми двумя .divide - пары на сегодня
        int dividerCount = 0;
        int todaysLecturesCount = 0;
        for (WebElement we : schedElements) {
            if (dividerCount == 2) {
                break;
            }
            if (we.getAttribute("class").equals("divide")) {
                dividerCount++;
            }
            if (we.getAttribute("class").equals("shedule-weekday-row")) {
                todaysLecturesCount++;
            }
        }
        return todaysLecturesCount;
    }
}
