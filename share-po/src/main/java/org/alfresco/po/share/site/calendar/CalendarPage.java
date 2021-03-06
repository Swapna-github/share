/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 * This file is part of Alfresco
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.po.share.site.calendar;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import org.alfresco.po.HtmlPage;
import org.alfresco.po.RenderTime;
import org.alfresco.po.RenderWebElement;
import org.alfresco.po.share.site.SitePage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * Site Calendar Page object
 * relating to Share site Calendar page
 * 
 * @author Marina.Nenadovets
 */

public class CalendarPage extends SitePage
{
    private Log logger = LogFactory.getLog(this.getClass());

    int waitInMilliSeconds = 2000;
    @RenderWebElement 
    @FindBy(id="HEADER_SITE_CALENDAR") private WebElement calendarHeader;
    private static final By ADD_EVENT = By.cssSelector("#template_x002e_toolbar_x002e_calendar_x0023_default-addEvent-button-button");
    private static final By TAB_CONTAINER = By.cssSelector("div[id*='defaultView']");

    // Buttons
    private static final By DAY_BUTTON = By.cssSelector("button[id$='_default-day-button']");
    private static final By WEEK_BUTTON = By.cssSelector("button[id$='_default-week-button']");
    private static final By MONTH_BUTTON = By.cssSelector("button[id$='_default-month-button']");
    private static final By AGENDA_BUTTON = By.cssSelector("button[id$='_default-agenda-button']");

    // Confirmation that expected tab opened
    private static final By DAY_TAB_TABLE = By.cssSelector("div[class='fc-view fc-view-agendaDay fc-agenda']");
    private static final By WEEK_TAB_TABLE = By.cssSelector("div[class='fc-view fc-view-agendaWeek fc-agenda']");
    private static final By MONTH_TAB_TABLE = By.cssSelector("div[class='fc-view fc-view-month fc-grid']");
    private static final By AGENDA_LINK_PREVIOUS = By.cssSelector("a[class='previousEvents agendaNav']");
    @SuppressWarnings("unused")
    private static final By AGENDA_LINK_NEXT = By.cssSelector("a[class='nextEvents agendaNav']");

    private final static String DAY_TAB_ADD_EVENT = "//tr[contains(@class,'fc-slot%s')]//div";
    private final static String WEEK_TAB_ADD_EVENT = "//div[contains(@class,'agendaWeek')]//table[@class='fc-agenda-slots']/tbody/tr[%s]/td/div";
    private final static String MONTH_TAB_ADD_EVENT = "//td[not(contains(@class,'fc-other-month'))]//div[@class='fc-day-content']/preceding-sibling::div[@class='fc-day-number' and text()='%s']";

    private static final By SHOW_ALL_HOURS_BUTTON = By.xpath("//button[@title='Show all hours']");
    private static final By SHOW_WORKING_HOURS_BUTTON = By.xpath("//button[@title='Show working hours']");

    private static final By EDIT_PANEL = By.cssSelector("div[id$='eventEditPanel-dialog']");
    private static final By INFO_PANEL = By.cssSelector("div[id$='eventInfoPanel_c']");

    private static final By ICALL_FED_LINK = By.cssSelector("a[id$='default-publishEvents-button-button']");

    // Tag panel
    private static final By SHOW_ALL_ITEMS_LINK = By.cssSelector("a[rel='-all-']");
    private final static String TAG_LINK = "//a[@class='tag-link' and @rel='%s']";

    /**
     * Enum for choose Calendar Tab or Add event button
     */
    public enum ActionEventVia
    {
        DAY_TAB(DAY_BUTTON), WEEK_TAB(WEEK_BUTTON), MONTH_TAB(MONTH_BUTTON), AGENDA_TAB(AGENDA_BUTTON), ADD_EVENT_BUTTON(ADD_EVENT);

        public final By contentLocator;

        ActionEventVia(By createEventVia)
        {
            this.contentLocator = createEventVia;

        }
    }

    /**
     * Enum for choose event type for some tab
     */
    public enum EventType
    {
        // Check links for all tabs
        // for DAY_TAB
        DAY_TAB_SINGLE_EVENT("//div[contains(@class,'agendaDay')]//div[contains(text(),'%s')]/../../parent::a"),
        DAY_TAB_ALL_DAY_EVENT("//div[contains(@class,'agendaDay')]//span[contains(text(),'%s')]/../parent::a"),
        DAY_TAB_MULTIPLY_EVENT("//div[contains(@class,'agendaDay')]//div[contains(text(),'%s')]/../../parent::a"),

        // for WEEK_TAB
        WEEK_TAB_SINGLE_EVENT("//div[contains(@class,'agendaWeek')]//div[contains(text(),'%s')]/../../parent::a"),
        WEEK_TAB_ALL_DAY_EVENT("//div[contains(@class,'agendaWeek')]//span[contains(text(),'%s')]/../parent::a"),
        WEEK_TAB_MULTIPLY_EVENT("//div[contains(@class,'agendaWeek')]//div[contains(text(),'%s')]/../../parent::a"),

        // for MONTH_TAB
        MONTH_TAB_SINGLE_EVENT("//div[contains(@class,'month')]//span[contains(text(),'%s')]/../parent::a"),
        MONTH_TAB_ALL_DAY_EVENT("//div[contains(@class,'month')]//span[contains(text(),'%s')]/../parent::a"),
        MONTH_TAB_MULTIPLY_EVENT("//div[contains(@class,'month')]//span[contains(text(),'%s')]/../parent::a"),

        // for AGENDA_TAB
        AGENDA_TAB_SINGLE_EVENT("//div[contains(@class,'agendaview')]//a[contains(text(),'%s')]"),
        AGENDA_TAB_ALL_DAY_EVENT("//div[contains(@class,'agendaview')]//a[contains(text(),'%s')]"),
        AGENDA_TAB_MULTIPLY_EVENT("//div[contains(@class,'agendaview')]//a[contains(text(),'%s')]");

        public String contentLocator;

        private EventType(String contentLocator)
        {
            this.contentLocator = contentLocator;
        }

        public String getXpathLocator()
        {
            return contentLocator;
        }
    }

    /**
     * Enum for check time during day
     */
    public enum HoursFromTable
    {
        // Check displayed hours
        FIRST_ALL_HOUR("//tr[contains(@class,'fc-slot0')]/th[contains(text(),'00:00')]"),
        FIRST_WORKING_HOUR("//tr[contains(@class,'fc-slot0')]/th[contains(text(),'07:00')]"),
        LAST_WORKING_HOUR("//tr[contains(@class,'fc-slot22')]/th[contains(text(),'18:00')]"),
        LAST_ALL_HOUR("//tr[contains(@class,'fc-slot46')]/th[contains(text(),'23:00')]");

        public String contentLocator;

        private HoursFromTable(String contentLocator)
        {
            this.contentLocator = contentLocator;
        }

        public String getXpathLocator()
        {
            return contentLocator;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public CalendarPage render(RenderTime timer)
    {
        basicRender(timer);
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public CalendarPage render()
    {
        return render(new RenderTime(maxPageLoadingTime));
    }

    /**
     * Method click on button 'Add event'
     * 
     * @return AddEventForm
     */
    public AddEventForm clickOnAddEvent()
    {
        try
        {
            findAndWait(ADD_EVENT).click();
            logger.info("Click add event button");
        }
        catch (NoSuchElementException e)
        {
            logger.debug("Unable to locate Add Event button");
        }
        catch (TimeoutException te)
        {
            logger.debug("The operation has timed out");
        }
        return factoryPage.instantiatePage(driver, AddEventForm.class);
    }

    /**
     * Method click on button 'Add event' or on Tab for adding event
     * 
     * @return AddEventForm
     */
    public AddEventForm clickOnTabOrButtonAddEvent(ActionEventVia createEventVia)
    {
        try
        {

            if (createEventVia == null)
            {
                throw new IllegalArgumentException("Tab Name is required");
            }

            if (createEventVia != null)
            {
                switch (createEventVia)
                {
                    case DAY_TAB:
                        String dayXpath = String.format(DAY_TAB_ADD_EVENT, Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
                        WebElement element1 = findAndWait(By.xpath(dayXpath));
                        element1.click();
                        break;
                    case WEEK_TAB:
                        String weekXpath = String.format(WEEK_TAB_ADD_EVENT, Calendar.getInstance().get(Calendar.HOUR_OF_DAY) * 2);
                        WebElement element2 = findAndWait(By.xpath(weekXpath));
                        element2.click();
                        break;
                    case MONTH_TAB:
                        String monthXpath = String.format(MONTH_TAB_ADD_EVENT, Calendar.getInstance().get(Calendar.DATE));
                        WebElement element3 = findAndWait(By.xpath(monthXpath));
                        element3.click();
                        break;
                    case AGENDA_TAB:
                        findAndWait(ADD_EVENT).click();
                        break;
                    case ADD_EVENT_BUTTON:
                        findAndWait(ADD_EVENT).click();
                        break;
                    default:
                        findAndWait(ADD_EVENT).click();
                        break;
                }
            }
        }
        catch (NoSuchElementException e)
        {
            logger.debug("Unable to locate Add Event button");
        }
        catch (TimeoutException te)
        {
            logger.debug("The operation has timed out");
        }
        return factoryPage.instantiatePage(driver, AddEventForm.class);
    }

    /**
     * Method for event creation
     * 
     * @return CalendarPage
     */
    public HtmlPage createEvent(String whatField, String whereField, String description, boolean allDay)
    {
        return createEvent(null, whatField, whereField, description, null, null, null, null, null, allDay);
    }

    /**
     * Method for event creation
     * 
     * @param createEventVia ActionEventVia
     * @param whatField String
     * @param whereField String
     * @param description String
     * @param startDate String
     * @param startTime String
     * @param endDate String
     * @param endTime String
     * @param tags String
     * @param allDay boolean
     * @return CalendarPage
     */

    public HtmlPage createEvent(ActionEventVia createEventVia, String whatField, String whereField, String description, String startDate, String startTime, String endDate, String endTime, String tags, boolean allDay)
    {
        return createEvent(createEventVia, whatField, whereField, description, null, null, startDate, startTime, null, null, endDate, endTime, tags, allDay);
    }

    /**
     * @param createEventVia ActionEventVia
     * @param whatField String
     * @param whereField String
     * @param description String
     * @param startYear String
     * @param startMonth String
     * @param startDate String
     * @param startTime String
     * @param endYear String
     * @param endMonth String
     * @param endDate String
     * @param endTime String
     * @param tags String
     * @param allDay boolean
     * @return CalendarPage
     */
    public HtmlPage createEvent(ActionEventVia createEventVia, String whatField, String whereField, String description, String startYear, String startMonth, String startDate, String startTime, String endYear, String endMonth, String endDate, String endTime, String tags, boolean allDay)
    {
        logger.info("Create event with name " + whatField);
        try
        {
            AddEventForm addEventForm;
            CalendarPage calendarPage = getCurrentPage().render();
            CalendarContainer calendarContainer = factoryPage.instantiatePage(driver, CalendarContainer.class);
            if (createEventVia != null)
            {
                switch (createEventVia)
                {
                    case DAY_TAB:
                        calendarPage = (CalendarPage) calendarPage.chooseDayTab();
                        addEventForm = calendarPage.clickOnTabOrButtonAddEvent(ActionEventVia.DAY_TAB);
                        break;
                    case WEEK_TAB:
                        calendarPage = (CalendarPage) calendarPage.chooseWeekTab();
                        addEventForm = calendarPage.clickOnTabOrButtonAddEvent(ActionEventVia.WEEK_TAB);
                        break;
                    case MONTH_TAB:
                        calendarPage = (CalendarPage) calendarPage.chooseMonthTab();
                        addEventForm = calendarPage.clickOnTabOrButtonAddEvent(ActionEventVia.MONTH_TAB);
                        break;
                    case AGENDA_TAB:
                        calendarPage = (CalendarPage) calendarPage.chooseAgendaTab();
                        addEventForm = calendarPage.clickOnTabOrButtonAddEvent(ActionEventVia.AGENDA_TAB);
                        break;
                    case ADD_EVENT_BUTTON:
                        addEventForm = calendarPage.clickOnAddEvent();
                        break;
                    default:
                        addEventForm = calendarPage.clickOnAddEvent();
                        break;
                }
            }
            else
            {
                addEventForm = calendarPage.clickOnAddEvent();
            }

            if (whatField != null && !whatField.isEmpty())
            {
                addEventForm.setWhatField(whatField);
            }
            if (whereField != null && !whereField.isEmpty())
            {
                addEventForm.setWhereField(whereField);
            }
            if (description != null && !description.isEmpty())
            {
                addEventForm.setDescriptionField(description);
            }
            if (description != null && !description.isEmpty())
            {
                addEventForm.setDescriptionField(description);
            }

            if (startYear != null && !startYear.isEmpty())
            {
                addEventForm.clickStartDatePicker();
                calendarContainer.setYear(startYear);
                calendarContainer.setDate("1");
            }

            if (startMonth != null && !startMonth.isEmpty())
            {
                addEventForm.clickStartDatePicker();
                calendarContainer.setMonth(startMonth);
                calendarContainer.setDate("1");

            }
            if (startDate != null && !startDate.isEmpty())
            {
                addEventForm.clickStartDatePicker();
                calendarContainer.setDate(startDate);
            }

            
            if (endYear != null && !endYear.isEmpty())
            {
                addEventForm.clickEndDatePicker();
                calendarContainer.setYear(endYear);
                calendarContainer.setDate("1");
            }
            
            if (endMonth != null && !endMonth.isEmpty())
            {
                addEventForm.clickEndDatePicker();
                calendarContainer.setMonth(endMonth);
                if (endDate != null && !endDate.isEmpty())
                {

                    calendarContainer.setDate(endDate);
                }
                else
                	calendarContainer.setDate("1");
            }
            else
            {
                if (endDate != null && !endDate.isEmpty())
                {

                    addEventForm.clickEndDatePicker();
                    calendarContainer.setDate(endDate);
                }
            }

            if (startTime != null && !startTime.isEmpty())
            {
                addEventForm.setStartTimeField(startTime);
            }

            if (endTime != null && !endTime.isEmpty())
            {
                addEventForm.setEndTimeField(endTime);
            }

            if (tags != null)
            {
                addEventForm.setTagsField(tags);
                addEventForm.clickAddTag();
            }

            if (allDay)
            {
                addEventForm.setAllDayCheckbox();

            }
            addEventForm.clickSave();
        }
        catch (TimeoutException te)
        {
            logger.debug("The operation has timed out");
        }
        catch (NoSuchElementException nse)
        {
            logger.debug("Unable to find the elements");
        }
        waitUntilElementDisappears(EDIT_PANEL, TimeUnit.SECONDS.convert(getDefaultWaitTime(), TimeUnit.MILLISECONDS));
        waitUntilNotVisible(By.cssSelector(".message"), "created", TimeUnit.SECONDS.convert(getDefaultWaitTime(), TimeUnit.MILLISECONDS));
        if (tags != null)
        {
            String tagXpath = String.format(TAG_LINK, tags.split(" ")[0]);
            waitUntilElementPresent(By.xpath(tagXpath), TimeUnit.SECONDS.convert(getDefaultWaitTime(), TimeUnit.MILLISECONDS));
        }
        return getCurrentPage();
    }

    /**
     * Method to verify whether Add Even Link is available
     * 
     * @return true if displayed
     */
    public boolean isAddEventPresent()
    {
        try
        {
            return findAndWait(ADD_EVENT, 2000).isDisplayed();
        }
        catch (TimeoutException nse)
        {
            return false;
        }
    }

    /**
     * Method for event edition
     * 
     * @param eventName String
     * @param eventType EventType
     * @param editEventVia ActionEventVia
     * @param whatField String
     * @param whereField String
     * @param description String
     * @param startDate String
     * @param startTime String
     * @param endDate String
     * @param endTime String
     * @param tags String
     * @param allDay boolean
     * @return CalendarPage
     */
    public HtmlPage editEvent(String eventName, EventType eventType, ActionEventVia editEventVia, String whatField, String whereField, String description, String startDate, String startTime, String endDate, String endTime, String tags, boolean allDay, String[] removeTag)
    {
        logger.info("Edit event with name " + whatField);
        try
        {
            EditEventForm editEventForm = null;
            CalendarPage calendarPage = getCurrentPage().render();
            CalendarContainer calendarContainer = factoryPage.instantiatePage(driver, CalendarContainer.class);
            InformationEventForm informationEventForm;
            if (editEventVia != null)
            {
                switch (editEventVia)
                {
                    case DAY_TAB:
                        calendarPage = (CalendarPage) calendarPage.chooseDayTab();
                        informationEventForm = calendarPage.clickOnEvent(eventType, eventName);
                        editEventForm = informationEventForm.clickOnEditEvent();
                        break;
                    case WEEK_TAB:
                        calendarPage = (CalendarPage) calendarPage.chooseWeekTab();
                        informationEventForm = calendarPage.clickOnEvent(eventType, eventName);
                        editEventForm = informationEventForm.clickOnEditEvent();
                        break;
                    case MONTH_TAB:
                        calendarPage = (CalendarPage) calendarPage.chooseMonthTab();
                        informationEventForm = calendarPage.clickOnEvent(eventType, eventName);
                        editEventForm = informationEventForm.clickOnEditEvent();
                        break;
                    case AGENDA_TAB:
                        calendarPage = (CalendarPage) calendarPage.chooseAgendaTab();
                        informationEventForm = calendarPage.clickOnEvent(eventType, eventName);
                        editEventForm = informationEventForm.clickOnEditEvent();
                        break;
                    default:
                        break;
                }
            }
            else
            {
                throw new IllegalArgumentException("Parameter editEventVia is required");
            }

            if (whatField != null && !whatField.isEmpty())
            {
                editEventForm.setWhatField(whatField);
            }
            if (whereField != null && !whereField.isEmpty())
            {
                editEventForm.setWhereField(whereField);
            }
            if (description != null && !description.isEmpty())
            {
                editEventForm.setDescriptionField(description);
            }
            if (description != null && !description.isEmpty())
            {
                editEventForm.setDescriptionField(description);
            }

            if (startDate != null && !startDate.isEmpty())
            {
                editEventForm.clickStartDatePicker();
                calendarContainer.setDate(startDate);
            }

            if (startTime != null && !startTime.isEmpty())
            {
                editEventForm.setStartTimeField(startTime);
            }

            if (endDate != null && !endDate.isEmpty())
            {
                editEventForm.clickEndDatePicker();
                calendarContainer.setDate(endDate);
            }

            if (endTime != null && !endTime.isEmpty())
            {
                editEventForm.setEndTimeField(endTime);
            }

            if (tags != null)
            {
                editEventForm.setTagsField(tags);
                editEventForm.clickAddTag();
            }

            if (allDay)
            {
                editEventForm.setAllDayCheckbox();
            }

            if (removeTag != null)
            {
                editEventForm.removeTag(removeTag);
            }

            editEventForm.clickSave();
        }
        catch (TimeoutException te)
        {
            logger.debug("The operation has timed out");
        }
        catch (NoSuchElementException nse)
        {
            logger.debug("Unable to find the elements");
        }
        waitUntilElementDisappears(EDIT_PANEL, TimeUnit.SECONDS.convert(getDefaultWaitTime(), TimeUnit.MILLISECONDS));
        if (tags != null)
        {
            String tagXpath = String.format(TAG_LINK, tags.split(" ")[0]);
            waitUntilElementPresent(By.xpath(tagXpath), TimeUnit.SECONDS.convert(getDefaultWaitTime(), TimeUnit.MILLISECONDS));
        }
        if (removeTag != null)
        {
            String tagXpath = String.format(TAG_LINK, removeTag[0]);
            waitUntilElementDisappears(By.xpath(tagXpath), TimeUnit.SECONDS.convert(getDefaultWaitTime(), TimeUnit.MILLISECONDS));
        }
        return getCurrentPage();
    }

    /**
     * Method for event deletion
     * 
     * @param eventName String
     * @param eventType EventType
     * @param editEventVia ActionEventVia
     * @return CalendarPage
     */
    public HtmlPage deleteEvent(String eventName, EventType eventType, ActionEventVia editEventVia)
    {
        logger.info("Delete event with name " + eventName);
        try
        {
            DeleteEventForm deleteEventForm;
            CalendarPage calendarPage = getCurrentPage().render();
            InformationEventForm informationEventForm;
            if (editEventVia != null)
            {
                switch (editEventVia)
                {
                    case DAY_TAB:
                        calendarPage = (CalendarPage) calendarPage.chooseDayTab();
                        informationEventForm = calendarPage.clickOnEvent(eventType, eventName);
                        deleteEventForm = informationEventForm.clickOnDeleteEvent();
                        deleteEventForm.confirmDeleteEvent();
                        break;
                    case WEEK_TAB:
                        calendarPage = (CalendarPage) calendarPage.chooseWeekTab();
                        informationEventForm = calendarPage.clickOnEvent(eventType, eventName);
                        deleteEventForm = informationEventForm.clickOnDeleteEvent();
                        deleteEventForm.confirmDeleteEvent();
                        break;
                    case MONTH_TAB:
                        calendarPage = (CalendarPage) calendarPage.chooseMonthTab();
                        informationEventForm = calendarPage.clickOnEvent(eventType, eventName);
                        deleteEventForm = informationEventForm.clickOnDeleteEvent();
                        deleteEventForm.confirmDeleteEvent();
                        break;
                    case AGENDA_TAB:
                        calendarPage = (CalendarPage) calendarPage.chooseAgendaTab();
                        informationEventForm = calendarPage.clickOnEvent(eventType, eventName);
                        deleteEventForm = informationEventForm.clickOnDeleteEvent();
                        deleteEventForm.confirmDeleteEvent();
                        break;
                    default:
                        break;
                }
            }
            else
            {
                throw new IllegalArgumentException("Parameter editEventVia is required");
            }
        }
        catch (TimeoutException te)
        {
            logger.debug("The operation has timed out");
        }
        catch (NoSuchElementException nse)
        {
            logger.debug("Unable to find the elements");
        }
        waitUntilElementDisappears(INFO_PANEL, TimeUnit.SECONDS.convert(getDefaultWaitTime(), TimeUnit.MILLISECONDS));
        waitUntilNotVisible(By.cssSelector(".message"), "was deleted", TimeUnit.SECONDS.convert(getDefaultWaitTime(), TimeUnit.MILLISECONDS));
        String linkEventXpath = String.format(eventType.getXpathLocator(), eventName);
        waitUntilElementDeletedFromDom(By.xpath(linkEventXpath), SECONDS.convert(maxPageLoadingTime, MILLISECONDS));
        return getCurrentPage();
    }

    /**
     * Choose day tab
     * 
     * @return HtmlPage
     */
    public HtmlPage chooseDayTab()
    {
        try
        {
            logger.info("Choose day tab");
            findAndWait(DAY_BUTTON).click();
        }
        catch (NoSuchElementException e)
        {
            logger.debug("Unable to locate Day button");
        }
        catch (TimeoutException te)
        {
            logger.debug("The operation has timed out");
        }
        waitForPageLoad(SECONDS.convert(maxPageLoadingTime, MILLISECONDS));
        synchronized (this)
        {
            try
            {
                this.wait(waitInMilliSeconds);
            }
            catch (InterruptedException e)
            {
            }
        }
        return getCurrentPage();
    }

    /**
     * Choose week tab
     * 
     * @return CalendarPage
     */
    public HtmlPage chooseWeekTab()
    {
        try
        {
            logger.info("Choose week tab");
            findAndWait(WEEK_BUTTON).click();
        }
        catch (NoSuchElementException e)
        {
            logger.debug("Unable to locate Week button");
        }
        catch (TimeoutException te)
        {
            logger.debug("The operation has timed out");
        }
        waitForPageLoad(SECONDS.convert(maxPageLoadingTime, MILLISECONDS));
        synchronized (this)
        {
            try
            {
                this.wait(waitInMilliSeconds);
            }
            catch (InterruptedException e)
            {
            }
        }
        return getCurrentPage();
    }

    /**
     * Choose month tab
     * 
     * @return CalendarPage
     */
    public HtmlPage chooseMonthTab()
    {
        try
        {
            logger.info("Choose month tab");
            findAndWait(MONTH_BUTTON).click();
        }
        catch (NoSuchElementException e)
        {
            logger.debug("Unable to locate Month button");
        }
        catch (TimeoutException te)
        {
            logger.debug("The operation has timed out");
        }
        waitForPageLoad(SECONDS.convert(maxPageLoadingTime, MILLISECONDS));
        synchronized (this)
        {
            try
            {
                this.wait(waitInMilliSeconds);
            }
            catch (InterruptedException e)
            {
            }
        }
        return getCurrentPage();
    }

    /**
     * Choose agenda tab
     * 
     * @return CalendarPage
     */
    public HtmlPage chooseAgendaTab()
    {
        try
        {
            logger.info("Choose agenda tab");
            findAndWait(AGENDA_BUTTON).click();
        }
        catch (NoSuchElementException e)
        {
            logger.debug("Unable to locate Agenda button");
        }
        catch (TimeoutException te)
        {
            logger.debug("The operation has timed out");
        }
        waitForPageLoad(SECONDS.convert(maxPageLoadingTime, MILLISECONDS));
        synchronized (this)
        {
            try
            {
                this.wait(waitInMilliSeconds);
            }
            catch (InterruptedException e)
            {
            }
        }
        return getCurrentPage();
    }

    /**
     * Check that day tab opened
     * 
     * @return true if day tab opened
     */
    public boolean isDayTabOpened()
    {
        try
        {
            logger.info("Check is day tab opened");
            WebElement dayTab = driver.findElement(DAY_TAB_TABLE);
            return dayTab.isDisplayed();
        }
        catch (NoSuchElementException te)
        {
            logger.debug("Unable to locate day tab table");
            return false;
        }
    }

    /**
     * Check that week tab opened
     * 
     * @return true if week tab opened
     */
    public boolean isWeekTabOpened()
    {
        try
        {
            logger.info("Check is week tab opened");
            WebElement weekTab = driver.findElement(WEEK_TAB_TABLE);
            return weekTab.isDisplayed();
        }
        catch (NoSuchElementException te)
        {
            logger.debug("Unable to locate week tab table");
            return false;
        }
    }

    /**
     * Check that month tab opened
     * 
     * @return true if month tab opened
     */
    public boolean isMonthTabOpened()
    {
        try
        {
            logger.info("Check is month tab opened");
            WebElement monthTab = driver.findElement(MONTH_TAB_TABLE);
            return monthTab.isDisplayed();
        }
        catch (NoSuchElementException te)
        {
            logger.debug("Unable to locate month tab table");
            return false;
        }
    }

    /**
     * Check that agenda tab opened
     * 
     * @return true if agenda tab opened
     */
    public boolean isAgendaTabOpened()
    {
        try
        {
            logger.info("Check is agenda tab opened");
            WebElement linkPreviousEvent = driver.findElement(AGENDA_LINK_PREVIOUS);
            return linkPreviousEvent.isDisplayed();
        }
        catch (NoSuchElementException te)
        {
            logger.debug("Unable to locate link for agenda tab");
            return false;
        }
    }

    /**
     * Check that event present
     * 
     * @return true if event present
     */
    public boolean isEventPresent(EventType eventType, String eventName)
    {
        try
        {
            logger.info("Check that link with name " + eventName + " presented at the current tab");
            if (!eventName.isEmpty())
            {
                waitUntilAlert();
                String linkEventXpath = String.format(eventType.getXpathLocator(), eventName);
                WebElement element = driver.findElement(By.xpath(linkEventXpath));
                return element.isDisplayed();
            }
            else
            {
                return false;
            }
        }
        catch (NoSuchElementException te)
        {
            logger.debug("Unable to locate expected link on tab table");
            return false;
        }
        catch (StaleElementReferenceException se)
        {
            return isEventPresent(eventType, eventName);
        }
    }

    /**
     * Method show All Hours
     * 
     * @return CalendarPage
     */
    public HtmlPage showAllHours()
    {
        try
        {
            logger.info("Click show all hours");
            findAndWait(SHOW_ALL_HOURS_BUTTON).click();
        }
        catch (NoSuchElementException e)
        {
            logger.debug("Unable to locate 'show all hours' button");
        }
        catch (TimeoutException te)
        {
            logger.debug("The operation has timed out");
        }
        return getCurrentPage();
    }

    /**
     * Check that button show All Hours displayed
     * 
     * @return true if button displayed
     */
    public boolean showAllHoursButtonDisplayed()
    {
        try
        {
            logger.info("Check 'show all hours' button displayed");
            WebElement button = driver.findElement(SHOW_ALL_HOURS_BUTTON);
            return button.isDisplayed();
        }
        catch (NoSuchElementException te)
        {
            logger.debug("Unable to locate 'show all hours' button");
            return false;
        }
    }

    /**
     * Method show Working Hours
     * 
     * @return CalendarPage
     */
    public HtmlPage showWorkingHours()
    {
        try
        {
            logger.info("Click show working hours");
            findAndWait(SHOW_WORKING_HOURS_BUTTON).click();
        }
        catch (NoSuchElementException e)
        {
            logger.debug("Unable to locate 'show working hours' button");
        }
        catch (TimeoutException te)
        {
            logger.debug("The operation has timed out");
        }
        return getCurrentPage();
    }

    /**
     * Check that button show Working Hours displayed
     * 
     * @return true if button displayed
     */
    public boolean showWorkingHoursButtonDisplayed()
    {
        try
        {
            logger.info("Check 'show working hours' button displayed");
            WebElement button = driver.findElement(SHOW_WORKING_HOURS_BUTTON);
            return button.isDisplayed();
        }
        catch (NoSuchElementException te)
        {
            logger.debug("Unable to locate 'show working hours' button");
            return false;
        }
    }

    /**
     * Check that element presented(e.g. for hour element )
     * 
     * @return String
     */
    public boolean checkTableHours(HoursFromTable xpath)
    {
        try
        {
            return driver.findElement(By.xpath(xpath.getXpathLocator())).isDisplayed();
        }
        catch (TimeoutException te)
        {
            throw new UnsupportedOperationException("Exceeded time to find the text.", te);
        }
    }

    /**
     * Check that element presented(e.g. for hour element )
     *
     * @param eventType EventType
     * @param eventName String
     * @return InformationEventForm
     */
    public InformationEventForm clickOnEvent(EventType eventType, String eventName)
    {
        try
        {
            if (isEventPresent(eventType, eventName))
            {
                logger.info("Click on event link with name " + eventName + "if it presented at the current tab");
                String linkEventXpath = String.format(eventType.getXpathLocator(), eventName);
                WebElement element = findAndWait(By.xpath(linkEventXpath));
                element.click();
            }
            else
            {
                throw new NoSuchElementException("Unable to locate expected link on tab table");
            }
        }
        catch (NoSuchElementException te)
        {
            logger.debug("Unable to locate expected link on tab table");
        }
        catch (StaleElementReferenceException se)
        {
            return clickOnEvent(eventType, eventName);
        }
        return factoryPage.instantiatePage(driver, InformationEventForm.class);
    }

    /**
     * Method return href for iCalFeed button
     * 
     * @return String
     */
    public String clickIcalFeedButton()
    {
        String iCallHref = null;
        try
        {

            logger.info("Click on iCallFeed link");
            WebElement element = findAndWait(ICALL_FED_LINK);
            element.click();

        }
        catch (NoSuchElementException te)
        {
            logger.debug("Unable to locate expected link on tab table");
        }
        return iCallHref;
    }

    /**
     * Method return href for iCalFeed button
     * 
     * @return String
     */
    public String getIcalFeedHref()
    {
        String iCallHref = null;
        try
        {

            logger.info("Get href for iCallFeed link");
            WebElement element = findAndWait(ICALL_FED_LINK);
            iCallHref = element.getAttribute("href");

        }
        catch (NoSuchElementException te)
        {
            logger.debug("Unable to locate expected link on tab table");
        }
        return iCallHref;
    }

    /**
     * Check that link 'Show all items' present
     * 
     * @return true if this link presented
     */
    public boolean isShowAllItemsPresent()
    {
        try
        {
            logger.info("Check is link 'Show all items' present");
            WebElement link = driver.findElement(SHOW_ALL_ITEMS_LINK);
            return link.isDisplayed();
        }
        catch (NoSuchElementException te)
        {
            logger.debug("Unable to locate link 'Show all items'");
            return false;
        }
    }

    /**
     * Method click link 'Show all items'
     * 
     * @return CalendarPage
     */
    public HtmlPage clickShowAllItems()
    {
        try
        {
            logger.info("Click link 'Show all items'");
            findAndWait(SHOW_ALL_ITEMS_LINK).click();
        }
        catch (NoSuchElementException e)
        {
            logger.debug("Unable to locate link 'Show all items'");
        }
        catch (TimeoutException te)
        {
            logger.debug("The operation has timed out");
        }

        synchronized (this)
        {
            try
            {
                this.wait(waitInMilliSeconds);
            }
            catch (InterruptedException e)
            {
            }
        }
        return getCurrentPage();
    }

    /**
     * Check that tag link present
     * 
     * @return true if tag link presented
     */
    public boolean isTagPresent(String tagName)
    {
        try
        {
            logger.info("Check is tag '" + tagName + "' present");
            String tagXpath = String.format(TAG_LINK, tagName);
            WebElement element = driver.findElement(By.xpath(tagXpath));
            return element.isDisplayed();
        }
        catch (NoSuchElementException te)
        {
            logger.debug("Unable to locate expected taf link " + tagName);
            return false;
        }
        catch (StaleElementReferenceException se)
        {
            return isTagPresent(tagName);
        }
    }

    /**
     * Method click link 'Show all items'
     * 
     * @return CalendarPage
     */
    public HtmlPage clickTagLink(String tagName)
    {
        String tagXpath;
        WebElement element;
        try
        {
            logger.info("Click tag link with name '" + tagName + "'");
            tagXpath = String.format(TAG_LINK, tagName);
            element = driver.findElement(By.xpath(tagXpath));
            element.click();
        }
        catch (NoSuchElementException e)
        {
            logger.debug("Unable to locate tag link with name '" + tagName + "'");
        }
        catch (TimeoutException te)
        {
            logger.debug("The operation has timed out");
        }

        synchronized (this)
        {
            try
            {
                this.wait(waitInMilliSeconds);
            }
            catch (InterruptedException e)
            {
            }
        }
        return getCurrentPage();
    }

    /**
     * Method to check whether it is possible to add an event
     * 
     * @param viaTab
     * @return boolean
     */
    public boolean isAddEventClickable(ActionEventVia viaTab)
    {
        if (viaTab == null)
        {
            throw new UnsupportedOperationException("Via Tab parameter is missing");
        }

        if (viaTab == ActionEventVia.AGENDA_TAB)
        {

            if (getTheNumOfEvents(ActionEventVia.AGENDA_TAB) > 0)
            {
                return false;
            }
            else
            {
                return isElementDisplayed(By.cssSelector("div[id*='defaultView']>span>a"));
            }
        }
        else
        {
            WebElement container = findAndWait(TAB_CONTAINER);
            return container.getAttribute("class").contains("calendar-editable");
        }
    }

    /**
     * Method to return the number of events
     * 
     * @param viaTab ActionEventVia
     * @return int
     */
    public int getTheNumOfEvents(ActionEventVia viaTab)
    {
        int size = 0;
        switch (viaTab)
        {
            case AGENDA_TAB:
                if (!isElementDisplayed(By.cssSelector("tbody[class*='data']>tr")))
                {
                    size = 0;
                }
                else
                {
                    size = driver.findElements(By.cssSelector("tbody[class*='data']>tr")).size();
                }
                break;
             default:
                 size = 0;
        }
        return size;
    }

	public void closeEventDialog() 
	{
		driver.findElement(By.cssSelector("div#eventInfoPanel a.container-close")).click();
	}
}
