package com.AutoReader.Auto;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class BookNavigator {
	public static void main(String[] args) {
		System.setProperty("webdriver.chrome.driver", "C:\\chromedriver-win64\\chromedriver.exe");
		ChromeOptions options = new ChromeOptions();
		options.setAcceptInsecureCerts(true);
		options.addArguments("--headless");
		WebDriver driver = new ChromeDriver(options);
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

		try {
			navigateSite(driver, wait);
		} finally {
			driver.quit();
		}
	}

	private static void navigateSite(WebDriver driver, WebDriverWait wait) {
		driver.get("https://education.launchcode.org/java-web-development/");
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".toctree-l1 > a")));

		List<WebElement> chapterLinks = driver.findElements(By.cssSelector(".toctree-l1 > a"));
		int totalChapters = chapterLinks.size();
		System.out.println("Total chapters found: " + totalChapters);

		for (int chapterIndex = 0; chapterIndex < totalChapters; chapterIndex++) {
			// Ensure chapter index is within bounds
			if (chapterIndex >= chapterLinks.size()) {
				System.out.println("Chapter index out of bounds: " + chapterIndex);
				break;
			}

			WebElement chapterLink = chapterLinks.get(chapterIndex);
			String chapterName = chapterLink.getText();
			String chapterUrl = chapterLink.getAttribute("href");
			System.out.println("Navigating to Chapter: " + chapterName + " (" + (chapterIndex + 1) + "/" + totalChapters + ")");

			driver.get(chapterUrl);

			// Check if sections exist in the chapter
			if (wait.until(ExpectedConditions.or(
					ExpectedConditions.presenceOfElementLocated(By.className("section")),
					ExpectedConditions.presenceOfElementLocated(By.tagName("body"))))) {

				List<WebElement> sections = driver.findElements(By.className("section"));
				int totalSections = sections.size();
				System.out.println("Total sections in chapter: " + totalSections);

				for (int i = 0; i < totalSections; i++) {
					// Ensure section index is within bounds
					if (i >= sections.size()) {
						System.out.println("Section index out of bounds: " + i);
						break;
					}

					WebElement section = sections.get(i);
					System.out.println("Reading Section " + (i + 1) + " in Chapter " + chapterName + ":");
					System.out.println(section.getText());

					// Perform the second action
					try {
						driver.get("https://education.launchcode.org/java-web-development/chapters/introduction-and-setup/why-java.html");
						WebElement specificParagraph = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("index-1")));
						System.out.println("Specific paragraph text: " + specificParagraph.getText());
					} catch (NoSuchElementException e) {
						System.out.println("Element not found: " + e.getMessage());
					} catch (TimeoutException e) {
						System.out.println("Timeout waiting for specific paragraph: " + e.getMessage());
					} catch (Exception e) {
						e.printStackTrace();
					}

					// Navigate back to the chapter page to continue reading sections
					driver.navigate().back();
					wait.until(ExpectedConditions.presenceOfElementLocated(By.className("section")));
					sections = driver.findElements(By.className("section"));
				}
			} else {
				System.out.println("No sections found in Chapter: " + chapterName);
			}

			// Retry navigating back to the main page if a TimeoutException occurs
			boolean mainPageLoaded = false;
			int attempts = 0;
			while (!mainPageLoaded && attempts < 3) {
				try {
					driver.navigate().back();
					wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".toctree-l1 > a")));
					chapterLinks = driver.findElements(By.cssSelector(".toctree-l1 > a"));  // Re-fetch chapter links
					totalChapters = chapterLinks.size();  // Update total chapters
					mainPageLoaded = true;
					System.out.println("Successfully navigated back to the main page.");
				} catch (TimeoutException e) {
					attempts++;
					System.out.println("Retrying navigation back to the main page: attempt " + attempts);
				} catch (Exception e) {
					System.out.println("Failed to navigate back to the main page: " + e.getMessage());
					break;
				}
			}

			if (!mainPageLoaded) {
				System.out.println("Failed to navigate back to the main page after 3 attempts");
				break;
			}
		}
	}
}
