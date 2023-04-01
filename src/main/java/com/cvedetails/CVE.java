package com.cvedetails;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.text.WordUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.time.Month;

public class CVE {

    static WebDriver driver;

    private static void setup() {
        driver = new HtmlUnitDriver();
    }

    private static void teardown() {
        if (driver != null)
            driver.quit();
    }

    JsonArray jsonArray = new JsonArray();
    static Gson gson = new Gson();
    String rows = "//tr[@class='srrowns']";
    String pages = "//div[@id='searchresults']//following-sibling::div[@class='paging']/a";
    String url = "https://www.cvedetails.com/vulnerabilities-by-types.php";

    String fileName = "cveDetails.json";

    void extractAll() throws StaleElementReferenceException, IOException {

        driver.get(url);
        String title = driver.getTitle();
        assertContains(title, "Vulnerability distribution of cve security vulnerabilities by types");

        List<WebElement> mainRowsList = driver.findElements(By.xpath("//table[@class='stats']/tbody/tr"));
        for (int i = 2; i < mainRowsList.size() - 1; i++) {
            //Click on Year Link
            WebElement yearLink = driver.findElement(By.xpath("//table[@class='stats']/tbody/tr[" + i + "]/th/a"));
            String year = yearLink.getText();
            yearLink.click();
            List<WebElement> pageList = driver.findElements(By.xpath(pages));

            for (int j = 0; j < pageList.size(); j++) {
                List<WebElement> rowList = driver.findElements(By.xpath(rows));
                List<WebElement> pagesList = driver.findElements(By.xpath(pages));
                appendToJSON(rowList);
                System.out.println("Page " + (j + 1) + " from " + year + " is added to the table");
                if (j != pageList.size() - 1) {
                    pagesList.get(j + 1).click();
                }
            }
            driver.get(url);
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
            driver.manage().timeouts().setScriptTimeout(Duration.ofSeconds(60));
        }
        FileWriter fileWriter = new FileWriter(fileName);
        System.out.println(jsonArray.toString());
        gson.toJson(jsonArray, fileWriter);
        fileWriter.close();

    }

    private static void assertContains(String found, String expected) {
        if (!found.contains(expected)) {
            throw new RuntimeException("page does not have expected title. got: " + expected);
        }
    }

    void extractByYearAndMonth(int year, String month) throws StaleElementReferenceException, IOException, SQLException {

        driver.get(url);
        String title = driver.getTitle();
        assertContains(title, "Vulnerability distribution of cve security vulnerabilities by types");
        //Click on Year Link
        driver.findElement(By.xpath("//table[@class='stats']/tbody/tr/following::a[contains(text(),'" + year + "')]")).click();
        //Click on Month Link
        driver.findElement(By.xpath("//a[contains(text(),'" + month + "')]")).click();
        List<WebElement> pageList = driver.findElements(By.xpath(pages));

        for (int j = 0; j < pageList.size(); j++) {
            List<WebElement> rowList = driver.findElements(By.xpath(rows));
            List<WebElement> pagesList = driver.findElements(By.xpath(pages));
            appendToJSON(rowList);
            System.out.println("Page " + (j + 1) + " from " + month + " " + year + " is added to the table");
            if (j != pageList.size() - 1) {
                pagesList.get(j + 1).click();
            }
        }
        FileWriter fileWriter = new FileWriter(fileName);
        System.out.println(jsonArray.toString());
        gson.toJson(jsonArray, fileWriter);
        fileWriter.close();
    }

    void extractByYear(int year) throws StaleElementReferenceException, IOException {

        driver.get(url);
        String title = driver.getTitle();
        assertContains(title, "Vulnerability distribution of cve security vulnerabilities by types");
        //Click on Year Link
        driver.findElement(By.xpath("//table[@class='stats']/tbody/tr/following::a[contains(text(),'" + year + "')]")).click();
        List<WebElement> pageList = driver.findElements(By.xpath(pages));

        for (int j = 0; j < pageList.size(); j++) {
            List<WebElement> rowList = driver.findElements(By.xpath(rows));
            List<WebElement> pagesList = driver.findElements(By.xpath(pages));
            appendToJSON(rowList);
            System.out.println("Page " + (j + 1) + " from " + year + " is added to the table");
            if (j != pageList.size() - 1) {
                pagesList.get(j + 1).click();
            }
        }
        FileWriter fileWriter = new FileWriter(fileName);
        System.out.println(jsonArray.toString());
        gson.toJson(jsonArray, fileWriter);
        fileWriter.close();

    }

    void appendToJSON(List<WebElement> rowList) throws StaleElementReferenceException {

        for (int i = 0; i < rowList.size(); i++) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("cveId", rowList.get(i).findElement(By.xpath("td[2]/a")).getText());
            try {
                jsonObject.addProperty("cweId", rowList.get(i).findElement(By.xpath("td[3]/a")).getText());
            } catch (NoSuchElementException e) {
            }
            jsonObject.addProperty("vulnerabilityType", rowList.get(i).findElement(By.xpath("td[5]")).getText());
            jsonObject.addProperty("publishDate", rowList.get(i).findElement(By.xpath("td[6]")).getText());
            jsonObject.addProperty("updateDate", rowList.get(i).findElement(By.xpath("td[7]")).getText());
            jsonObject.addProperty("score", rowList.get(i).findElement(By.xpath("td[8]/div")).getText());
            jsonArray.add(jsonObject);
        }
    }

    private static String convertToTitleCase(String text) {
        return WordUtils.capitalizeFully(text);
    }

    public static void main(String[] args) throws IOException, SQLException {
        String mode = System.getenv("COLLECTION_MODE");

        if (mode == null || mode.isBlank()) {
            mode = "current";
        }
        CVE cve = new CVE();
        setup();
        if (mode.equalsIgnoreCase("current")) {
            Year year = Year.from(LocalDate.now());
            Month month = Month.from(LocalDate.now());
            cve.extractByYearAndMonth(year.getValue(), convertToTitleCase(month.name()));
        } else if (mode.equalsIgnoreCase("all")) {
            cve.extractAll();
        } else if (mode.matches("[0-9]+")) {
            cve.extractByYear(Integer.parseInt(mode));
        }
        teardown();
    }

}