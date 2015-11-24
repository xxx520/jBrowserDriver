/* 
 * jBrowserDriver (TM)
 * Copyright (C) 2014-2015 Machine Publishers, LLC
 * ops@machinepublishers.com | screenslicer.com | machinepublishers.com
 * Cincinnati, Ohio, USA
 *
 * You can redistribute this program and/or modify it under the terms of the GNU Affero General Public
 * License version 3 as published by the Free Software Foundation.
 *
 * "ScreenSlicer", "jBrowserDriver", "Machine Publishers", and "automatic, zero-config web scraping"
 * are trademarks of Machine Publishers, LLC.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License version 3 for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License version 3 along with this
 * program. If not, see http://www.gnu.org/licenses/
 * 
 * For general details about how to investigate and report license violations, please see
 * https://www.gnu.org/licenses/gpl-violation.html and email the author, ops@machinepublishers.com
 */
package com.machinepublishers.jbrowserdriver;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.http.client.CookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver.ImeHandler;
import org.openqa.selenium.WebDriver.Timeouts;
import org.openqa.selenium.WebDriver.Window;
import org.openqa.selenium.logging.Logs;

class Options implements org.openqa.selenium.WebDriver.Options {
  private final ImeHandler imeHandler = new com.machinepublishers.jbrowserdriver.ImeHandler();
  private final AtomicReference<com.machinepublishers.jbrowserdriver.Logs> logs;
  private final AtomicReference<com.machinepublishers.jbrowserdriver.Window> window;
  private final CookieStore cookieStore;
  private final AtomicReference<com.machinepublishers.jbrowserdriver.Timeouts> timeouts;

  Options(final AtomicReference<com.machinepublishers.jbrowserdriver.Window> window,
      AtomicReference<com.machinepublishers.jbrowserdriver.Logs> logs,
      final CookieStore cookieStore,
      final AtomicReference<com.machinepublishers.jbrowserdriver.Timeouts> timeouts) {
    this.window = window;
    this.logs = logs;
    this.cookieStore = cookieStore;
    this.timeouts = timeouts;
  }

  private static org.apache.http.cookie.Cookie convert(Cookie in) {
    BasicClientCookie out = new BasicClientCookie(in.getName(), in.getValue());
    out.setDomain(in.getDomain());
    if (in.getExpiry() != null) {
      out.setExpiryDate(in.getExpiry());
    }
    out.setPath(in.getPath());
    out.setSecure(in.isSecure());
    out.setValue(in.getValue());
    out.setVersion(1);
    return out;
  }

  private static Cookie convert(org.apache.http.cookie.Cookie in) {
    return new Cookie(in.getName(),
        in.getValue(),
        in.getDomain(),
        in.getPath(),
        in.getExpiryDate(),
        in.isSecure());
  }

  @Override
  public void addCookie(Cookie cookie) {
    cookieStore.addCookie(convert(cookie));
  }

  @Override
  public void deleteAllCookies() {
    cookieStore.clear();
  }

  @Override
  public void deleteCookie(Cookie cookie) {
    List<org.apache.http.cookie.Cookie> cookies = cookieStore.getCookies();
    String toDelete = cookie.getDomain().toLowerCase()
        + "\n" + cookie.getName().toLowerCase()
        + "\n" + cookie.getPath().toLowerCase();
    for (org.apache.http.cookie.Cookie cur : cookies) {
      String curString = cur.getDomain().toLowerCase()
          + "\n" + cur.getName().toLowerCase()
          + "\n" + cur.getPath().toLowerCase();
      if (toDelete.equals(curString)) {
        removeFromCookieStore(cur);
      }
    }
  }

  private void removeFromCookieStore(org.apache.http.cookie.Cookie cookie) {
    BasicClientCookie tmp = new BasicClientCookie(cookie.getName(), "");
    tmp.setDomain(cookie.getDomain());
    tmp.setPath(cookie.getPath());
    tmp.setExpiryDate(new Date(0));
    cookieStore.addCookie(tmp);
  }

  @Override
  public void deleteCookieNamed(String name) {
    for (org.apache.http.cookie.Cookie cur : cookieStore.getCookies()) {
      if (cur.getName().equals(name)) {
        removeFromCookieStore(cur);
      }
    }
  }

  @Override
  public Cookie getCookieNamed(String name) {
    for (org.apache.http.cookie.Cookie cur : cookieStore.getCookies()) {
      if (cur.getName().equals(name)) {
        return convert(cur);
      }
    }
    return null;
  }

  @Override
  public Set<Cookie> getCookies() {
    Set<Cookie> cookies = new LinkedHashSet<Cookie>();
    for (org.apache.http.cookie.Cookie cur : cookieStore.getCookies()) {
      cookies.add(convert(cur));
    }
    return cookies;
  }

  @Override
  public ImeHandler ime() {
    return imeHandler;
  }

  @Override
  public Logs logs() {
    return logs.get();
  }

  @Override
  public Timeouts timeouts() {
    return timeouts.get();
  }

  @Override
  public Window window() {
    return window.get();
  }
}