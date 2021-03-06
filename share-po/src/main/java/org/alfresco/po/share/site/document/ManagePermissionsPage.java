/*
 * Copyright (C) 2005-2013s Alfresco Software Limited.
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

package org.alfresco.po.share.site.document;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.po.HtmlPage;
import org.alfresco.po.RenderElement;
import org.alfresco.po.RenderTime;
import org.alfresco.po.exception.PageException;
import org.alfresco.po.exception.PageOperationException;
import org.alfresco.po.share.SharePage;
import org.alfresco.po.share.site.document.UserSearchPage;
import org.alfresco.po.share.enums.UserRole;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Page object for Manage Permissions at granular level.
 *
 * @author Abhijeet Bharade
 * @since 1.7.0
 */
public class ManagePermissionsPage extends SharePage
{
    private final By lastRowPermissions = By.cssSelector("div[id$='_default-inheritedPermissions']");
    protected static final By addUserButton = By.cssSelector("div.add-user-group button");
    private final By saveButtonLocator = By.cssSelector("button[id$='-okButton-button']");
    private final By cancelButton = By.cssSelector("button[id$='-cancelButton-button']");
    protected static final By inheritPermissionButton = By.cssSelector("div[id$='_default-inheritedButtonContainer']");
    private final By inheritPermissionTable = By.cssSelector("div[id$='_default-inheritedPermissions']");
    private final By locallyPermissionTable = By.cssSelector("div[id$='_default-directPermissions']");
    private final By accessTypeButton = By.cssSelector("span[id^='roles-'] button");
    private final By userListLocator = By.cssSelector("div[id$='default-directPermissions'] tr[class^='yui-dt-rec']");
    private final By userNameLocator = By.cssSelector("td[class$='displayName']");
    private final By userRoleLocator = By.cssSelector("td[class*='role']");
    private final By listUserRole = By.cssSelector("span[id^='roles-']>div>div>ul>li");
    private final By listRolesWithSites = By.cssSelector("div[id$='_default-inheritedPermissions'] tr[class^='yui-dt-rec']");
    private final By listUsersGroups = By.cssSelector("td[class$='yui-dt-col-displayName']");
    private final By listRoleLocator = By.cssSelector("td[class*='yui-dt-col-role'] div");
    private final By userListInhrtPerm = By.cssSelector("div[id$='default-inheritedPermissions'] tr[class^='yui-dt-rec']");
    private final By deleteAction = By.cssSelector("td[class*='yui-dt-col-actions'] div div.action-set");
    private final By areYouSureButtonGroup = By.cssSelector("span.button-group span span button");
    private final By userPermissionDeleteAction = By.cssSelector("a[class$='action-link']");
    private final String userRowLocator = "//div[contains(@id, 'default-directPermissions')]//td/div[contains(text(),'%s')]/../..";
    private int retryCount = 0;

    public enum ButtonType
    {
        Yes, No;
    }


    private final Log logger = LogFactory.getLog(ManagePermissionsPage.class);

    @SuppressWarnings("unchecked")
    public ManagePermissionsPage render()
    {
        RenderTime timer = new RenderTime(maxPageLoadingTime);
        try
        {
            elementRender(timer, RenderElement.getVisibleRenderElement(addUserButton));
            elementRender(timer, RenderElement.getVisibleRenderElement(saveButtonLocator));
            elementRender(timer, RenderElement.getVisibleRenderElement(cancelButton));
            if (driver.findElement(inheritPermissionButton).getAttribute("class").contains("on"))
            {
                elementRender(timer, RenderElement.getVisibleRenderElement(lastRowPermissions));
            }
        }
        catch (NoSuchElementException e)
        {
        }
        return this;
    }

    /**
     * Mimics the action of clicking on Add user button.
     *
     * @return UserSearchPage
     */
    public UserSearchPage selectAddUser()
    {
        if (logger.isTraceEnabled())
        {
            logger.trace(" - Trying to click Add User button - ");
        }
        driver.findElement(addUserButton).click();
        return factoryPage.instantiatePage(driver, UserSearchPage.class);
    }

    /**
     * @param areYouSure ButtonType
     */
    private void clickAreYouSureDialogue(ButtonType areYouSure)
    {
        for (WebElement button : driver.findElements(areYouSureButtonGroup))
        {
            if (areYouSure.toString().equals(button.getText()))
            {
                button.click();
            }
        }
    }

    /**
     * @param turnOn boolean
     * @param areYouSure ButtonType
     * @return ManagePermissionsPage
     */
    public ManagePermissionsPage toggleInheritPermission(boolean turnOn, ButtonType areYouSure)
    {
        if (logger.isTraceEnabled())
        {
            logger.trace(" - Trying to click Inherit permissions button - ");
        }
        boolean buttonStatusOn = getInheritButtonStatus();
        WebElement inheritButton = driver.findElement(inheritPermissionButton);

        // click the button iff (turnOn = true and inherited permissions is off)
        if (turnOn && !buttonStatusOn)
        {
            inheritButton.findElement(By.cssSelector("button")).click();
        }

        // click the button iff (turnOn = false and inherited permissions is on)
        if (!turnOn && buttonStatusOn)
        {
            inheritButton.findElement(By.cssSelector("button")).click();
            try
            {
                // if the confirm Yes/No box appears select yes. This happens only first time.
                clickAreYouSureDialogue(areYouSure);
            }
            catch (Exception e)
            {
                // ignore. this confirm box does not appear everytime.
            }
        }
        return getCurrentPage().render();
    }

    /**
     * Resolves the inherit permission button to 'on' or 'off'
     *
     * @return boolean
     */
    private boolean getInheritButtonStatus()
    {
        try
        {
            findAndWait(By.cssSelector("div[class$='inherited-on']"), getDefaultWaitTime());
            return true;
        }
        catch (TimeoutException e)
        {
        }
        return false;
    }

    /**
     * Clicks on save and returns the page from where user arrived to this page.
     *
     * @return boolean
     */
    public boolean isInheritPermissionEnabled()
    {
        try
        {
            return findAndWait(inheritPermissionTable, 500).isDisplayed();
        }
        catch (TimeoutException e)
        {
            return false;
        }
    }

    /**
     * @return boolean
     */
    public boolean isLocallyPermissionEnabled()
    {
        try
        {
            return findAndWait(locallyPermissionTable, 500).isDisplayed();
        }
        catch (TimeoutException e)
        {
            return false;
        }
    }

    /**
     * Clicks on save and returns the page from where user arrived to this page.
     *
     * @return HtmlPage
     */
    public HtmlPage selectSave()
    {
        WebElement saveButton = findAndWait(saveButtonLocator);
        String saveButtonId = saveButton.getAttribute("id");
        saveButton.click();
        try
        {
            waitUntilElementDeletedFromDom(By.id(saveButtonId), SECONDS.convert(maxPageLoadingTime, MILLISECONDS));
        }
        catch (TimeoutException e)
        {
        }
        return getCurrentPage();
    }

    /**
     * Clicks on cancel and returns the page from where user arrived to this
     * page.
     *
     * @return HtmlPage
     */
    public HtmlPage selectCancel()
    {
        findAndWait(cancelButton).click();
        waitForPageLoad(SECONDS.convert(maxPageLoadingTime, MILLISECONDS));
        return getCurrentPage();
    }

    /**
     * From the drop down, the access level is selected for the first user.
     * <br>
     * depricated Use {@link ManagePermissionsPage setAccessType(UserProfile, UserRole)} or {@link ManagePermissionsPage setAccessType(String, UserRole)}
     * @param userRole UserRole
     */
    @Deprecated
    public void setAccessType(UserRole userRole)
    {
        if (null == userRole)
        {
            logger.info("Access type was null. Should be set to some level.");
            throw new UnsupportedOperationException("Access type cannot be null");
        }
        findAndWait(accessTypeButton).click();
        getRoleOption(driver, userRole).click();
    }

    /**
     * From the drop down, the access level is selected for the specified user.
     *
     * @param userProfile UserProfile
     * @param userRole UserRole
     */
    public void setAccessType(UserProfile userProfile, UserRole userRole)
    {
        if (null == userProfile)
        {
            logger.info("User Profile is null. Should be specified.");
            throw new IllegalArgumentException("UserProfile cannot be null");
        }
        String userName = userProfile.getfName() + " " + userProfile.getlName();
        userName = userName.trim();
        setAccessType(userName, userRole);
    }

    /**
     * From the drop down, the access level is selected for the specified user or group.
     *
     * @param name     The name of the user or group as it appears on screen including spaces.
     * @param userRole UserRole
     */
    public void setAccessType(String name, UserRole userRole)
    {
        if (null == userRole)
        {
            logger.info("Access type was null. Should be set to some level.");
            throw new IllegalArgumentException("Access type cannot be null");
        }
        if (null == name)
        {
            logger.info("Group Name is null. Should be specified.");
            throw new IllegalArgumentException("Name cannot be null");
        }

        String userSpecificAccess = "//div[starts-with(text(), '" + name + "')]/../..//span[contains(@id,'roles')] //button";
        By accessButtonSpecific = By.xpath(userSpecificAccess);
        try
        {
            findAndWait(accessButtonSpecific).click();
        }
        catch (TimeoutException te)
        {
            throw new PageOperationException("Unable to find Access Specific Button", te);
        }

        getRoleOption(driver, userRole).click();
    }

    /**
     * Check if user is already added for permission.
     *
     * @param name - First name or last name or full name <fName><space><lName>
     * @return boolean
     */
    public boolean isUserExistForPermission(String name)
    {
        boolean isExist = false;
        try
        {
            List<WebElement> userList = findAndWaitForElements(userListLocator, 10000);
            for (WebElement webElement : userList)
            {
                if (webElement.findElement(userNameLocator).getText().contains(name))
                {
                    isExist = true;
                }
            }
            return isExist;
        }
        catch (TimeoutException toe)
        {
            logger.warn("User name element is not found!!", toe);
            return isExist;
        }
    }

    /**
     * Check if user is already added for permission.
     *
     * @param name - First name or last name or full name <fName><space><lName>
     * @return UserRole
     */
    public UserRole getExistingPermissionForInheritPermission(String name)
    {
        try
        {
            List<WebElement> userList = findAndWaitForElements(userListInhrtPerm);
            for (WebElement webElement : userList)
            {
                if (webElement.findElement(userNameLocator).getText().contains(name))
                {
                    String currentRole = webElement.findElement(userRoleLocator).getText().toUpperCase();
                    if (currentRole.split("").length > 1)
                    {
                        currentRole = StringUtils.replace(currentRole.trim(), " ", "");
                    }
                    return UserRole.valueOf(currentRole);
                }
            }
        }
        catch (TimeoutException toe)
        {
            logger.error("User name elementis not found!!", toe);
        }
        throw new PageOperationException("User name is not found!!");
    }

    /**
     * Get existing permission for user/group
     *
     * @param name - First name or last name or full name <fName><space><lName>
     * @return UserRole
     */
    public UserRole getExistingPermission(String name)
    {
        try
        {
            List<WebElement> userList = findAndWaitForElements(userListLocator);
            for (WebElement webElement : userList)
            {
                if (webElement.findElement(userNameLocator).getText().contains(name))
                {
                    String currentRole = webElement.findElement(userRoleLocator).getText().toUpperCase();
                    return UserRole.valueOf(StringUtils.replace(currentRole, " ", ""));
                }
            }
        }
        catch (TimeoutException toe)
        {
            logger.error("User name elementis not found!!", toe);
        }
        throw new PageOperationException("User name is not found!!");
    }

    /**
     * Update role of existing Users in permission table.
     *
     * @param userName String
     * @param userRole UserRole
     * @return boolean
     */
    public boolean updateUserRole(String userName, UserRole userRole)
    {
        try
        {
            List<WebElement> elements = findAndWaitForElements(userListLocator, getDefaultWaitTime());
            for (WebElement webElement : elements)
            {
                if (webElement.findElement(userNameLocator).getText().contains(userName))
                {
                    WebElement roleElement = webElement.findElement(userRoleLocator);
                    roleElement.findElement(accessTypeButton).click();

                    selectRole(userRole);
                    return true;
                }
            }
        }
        catch (TimeoutException toe)
        {
            logger.error("User name is not found!!", toe);
        }
        catch (NoSuchElementException nse)
        {
            logger.error("Role element is not found", nse);
        }
        throw new PageOperationException("User or Role doesnt exist!!");
    }

    /**
     * Delete user or group from permission table.
     *
     * @param name String
     * @param role UserRole
     * @return boolean
     */
    public boolean deleteUserOrGroupFromPermission(String name, UserRole role)
    {
        try
        {
            List<WebElement> userList = findAndWaitForElements(userListLocator);
            for (WebElement webElement : userList)
            {
                if (webElement.findElement(userNameLocator).getText().contains(name))
                {
                    if (role.getRoleName().equalsIgnoreCase(webElement.findElement(userRoleLocator).findElement(accessTypeButton).getText()))
                    {
                        mouseOver(webElement.findElement(By.xpath("//td[contains(@class, 'yui-dt-col-actions')]/div")));
                        WebElement deleteDivElement = webElement.findElement(deleteAction);
                        driver.findElement(By.id(deleteDivElement.getAttribute("id"))).findElement(By.cssSelector("a")).click();
                        selectSave();
                        return true;
                    }
                }
            }
        }
        catch (TimeoutException toe)
        {
            logger.error("User name elementis not found!!", toe);
        }
        throw new PageOperationException("User name is not found!!");
    }

    /**
     * @param userRole UserRole
     */
    private void selectRole(UserRole userRole)
    {
        try
        {
            List<WebElement> elements = driver.findElements(listUserRole);
            for (WebElement webElement : elements)
            {
                if (userRole.getRoleName().equals(webElement.getText()))
                {
                    webElement.click();
                    return;
                }
            }
        }
        catch (NoSuchElementException nse)
        {
            logger.error("Roles element is not found", nse);
        }
        throw new PageOperationException("Role doesnt exist!!");
    }

    private List<String> getUserRoles()
    {
        List<String> userRoleStrings = new ArrayList<String>();
        try
        {
            List<WebElement> elements = driver.findElements(listUserRole);
            for (WebElement webElement : elements)
            {
                userRoleStrings.add(webElement.getText());
            }
            return userRoleStrings;
        }
        catch (NoSuchElementException nse)
        {
            logger.error("Roles element is not found", nse);
        }
        throw new PageOperationException("Role doesnt exist!!");
    }


    /**
     * Get available roles for existing users.
     *
     * @param userName String
     * @return List<String>
     */
    public List<String> getListOfUserRoles(String userName)
    {
        List<String> allRoles = new ArrayList<String>();
        try
        {
            List<WebElement> elements = findAndWaitForElements(userListLocator, getDefaultWaitTime());
            for (WebElement webElement : elements)
            {
                if (webElement.findElement(userNameLocator).getText().contains(userName))
                {
                    WebElement roleElement = webElement.findElement(userRoleLocator);
                    roleElement.findElement(accessTypeButton).click();

                    allRoles = getUserRoles();

                    roleElement.findElement(accessTypeButton).click();
                    return allRoles;
                }
            }
        }
        catch (TimeoutException toe)
        {
            logger.error("User name is not found!!", toe);
        }
        catch (NoSuchElementException nse)
        {
            logger.error("Role element is not found", nse);
        }
        throw new PageOperationException("User or Role doesnt exist!!");
    }

    /**
     * From the drop down, get the access level selected.
     */
    public UserRole getAccessType()
    {
        String role = findAndWait(accessTypeButton).getText();
        return UserRole.getUserRoleforName(role);
    }

    /**
     * Checks whether a user has direct permissions present.
     *
     * @param userProfile UserProfile
     * @return boolean
     */
    public boolean isDirectPermissionForUser(UserProfile userProfile)
    {
        List<WebElement> userPermissionRows = null;
        try
        {
            userPermissionRows = driver.findElements(By.cssSelector("div[id$='default-directPermissions'] tbody.yui-dt-data tr"));
        }
        catch (Exception e)
        {
            return false;
        }

        for (WebElement userPermissionRow : userPermissionRows)
        {
            String name = userPermissionRow.findElement(By.cssSelector("td[class$='-displayName']")).getText();
            if (StringUtils.equalsIgnoreCase(name, (userProfile.getfName()).trim()))
            {
                return true;
            }
        }
        return false;
    }


    /**
     * Finds the CSS for user role and clicks it option.
     *
     * @param driver
     * @param userRole
     * @return
     */
    private WebElement getRoleOption(WebDriver driver, UserRole userRole)
    {
        List<WebElement> options = findAndWaitForElements(By.cssSelector("div.bd li"));
        for (WebElement role : options)
        {
            if (userRole.getRoleName().equalsIgnoreCase(role.getText()))
            {
                return role;
            }
        }
        throw new PageException("Role option not found.");
    }

    /**
     * Get Inherited Site permission table in map key value pair.
     *
     * @return Map
     */
    public Map<String, String> getInheritedPermissions()
    {
        try
        {
            Map<String, String> usersAndPermissions = new HashMap<String, String>();
            List<WebElement> rowsOfInheritedPermission = findAndWaitForElements(listRolesWithSites);
            for (WebElement webElement : rowsOfInheritedPermission)
            {
                usersAndPermissions.put(webElement.findElement(listUsersGroups).getText(), webElement.findElement(listRoleLocator).getText());
            }
            return usersAndPermissions;
        }
        catch (TimeoutException toe)
        {
            throw new PageException("Element not found:" + listRolesWithSites, toe);
        }
        catch (NoSuchElementException nse)
        {
            throw new PageException("Element not found:" + listUsersGroups + " Or " + listRoleLocator, nse);
        }
    }

    /**
     * Verifies the delete button for the user is present in the page object.
     *
     * @return boolean
     */
    public boolean isUserDeleteButtonPresent(String name)
    {
        boolean isExist = false;
        try
        {
            List<WebElement> userList = findAndWaitForElements(userListLocator);
            for (WebElement webElement : userList)
            {
                if (webElement.findElement(userNameLocator).getText().contains(name))
                {
                    if (webElement.findElement(deleteAction).isEnabled())
                    {
                        isExist = true;
                    }
                }
            }
            return isExist;
        }
        catch (TimeoutException toe)
        {
            logger.error("User delete element is not found!!", toe);
            return isExist;
        }
    }

    /**
     * @param name String
     * @param role UserRole
     * @return WebElement
     */
    private WebElement getDeleteAction(String name, UserRole role)
    {
        try
        {
            List<WebElement> userList = findAndWaitForElements(userListLocator);
            for (WebElement webElement : userList)
                if (webElement.findElement(userNameLocator).getText().contains(name))
                {
                    String currentRole = StringUtils.replace(webElement.findElement(userRoleLocator).getText().toUpperCase(), " ", "");
                    if (role.equals(UserRole.valueOf(currentRole)))
                    {
                        mouseOver(webElement);
                        return webElement.findElement(userPermissionDeleteAction);
                    }
                }
        }
        catch (NoSuchElementException toe)
        {
            logger.error("User name elementis not found!!", toe);
            return null;
        }
        throw new PageOperationException("User name is not found!!");
    }

    /**
     * Check if delete action is present for the user and permission.
     *
     * @param name String
     * @param role UserRole
     * @return boolean
     */
    public boolean isDeleteActionPresent(String name, UserRole role)
    {
        WebElement element = getDeleteAction(name, role);
        if (null != element)
        {
            return true;

        }
        return false;
    }

    /**
     * Delete the user and permission.
     *
     * @param name String
     * @param role UserRole
     * @return ManagePermissionsPage
     */
    public HtmlPage deleteUserWithPermission(String name, UserRole role)
    {
        try
        {
            WebElement element = getDeleteAction(name, role);
            if (null != element)
            {
                element.click();
            }
        }
        catch (ElementNotVisibleException e)
        {
            deleteUserWithPermission(name, role);
            retryCount++;
            if (retryCount == 3)
            {
                throw new PageOperationException("Not able to locate delete button", e);
            }
        }
        return getCurrentPage();
    }

    /**
     * Method to return role for given userName
     *
     * @param userName String
     * @return UserRole
     */
    public UserRole getUserRole(String userName)
    {
        try
        {
            WebElement userRow = findAndWait(By.xpath(String.format(userRowLocator, userName)));
            String theRole = userRow.findElement(By.xpath("//td[contains(@class, 'role')]//button")).getText();
            for (UserRole allTheRoles : UserRole.values())
            {
                if (allTheRoles.getRoleName().equals(theRole))
                {
                    return allTheRoles;
                }
            }
        }
        catch (TimeoutException | NoSuchElementException e)
        {
            throw new PageOperationException("Unable to find either row locator for given name, or Role drop down", e);
        }
        throw new PageOperationException("Unable to find the matching role for user");
    }
}
