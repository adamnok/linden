/*
 * MIT LICENCE
 * 
 * Copyright (c) 2021 Adam Nok [adamnok@protonmail.com]
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package linden.plugin.plugins.electron.model

import sbt.pathToPathOps

import java.nio.file.{Files, Path}
import scala.concurrent.{ExecutionContext, Future}

object MainJs {
  def apply(
    isDevelopMode: Boolean = true,
    windowWidth: Int = 870,
    windowHeight: Int = 820,
    indexHtmlPath: String = "oolie/index.html"
  ) = new MainJs(
    isDevelopMode = isDevelopMode,
    windowWidth = windowWidth,
    windowHeight = windowHeight,
    indexHtmlPath = indexHtmlPath
  )
}

class MainJs(
  isDevelopMode: Boolean,
  windowWidth: Int,
  windowHeight: Int,
  indexHtmlPath: String
) {

  def render(targetDirectory: Path)(implicit ec: ExecutionContext): Future[Unit] = {
    val content =
      s"""
         |const { app, BrowserWindow, ipcMain } = require('electron')
         |const { rootPath } = require("electron-root-path")
         |const fs = require('fs')
         |
         |function createWindow () {
         |  const window = new BrowserWindow({
         |    width: 870,
         |    height: 820,
         |    webPreferences: {
         |      nodeIntegration: true
         |    }
         |  })
         |  window.setMenuBarVisibility(false)
         |  ${if (isDevelopMode) "window.webContents.openDevTools()" else ""}
         |  window.loadFile('$indexHtmlPath')
         |}
         |
         |app.whenReady().then(createWindow)
         |
         |app.on('window-all-closed', () => {
         |  if (process.platform !== 'darwin') {
         |    app.quit()
         |  }
         |})
         |
         |app.on('activate', () => {
         |  if (BrowserWindow.getAllWindows().length === 0) {
         |    createWindow()
         |  }
         |})
         |""".stripMargin
    val targetPath = targetDirectory / "oolie" / "electron-index-main.js"
    Files.createDirectories(targetPath.getParent)
    Files.createFile(targetPath)
    Files.writeString(targetPath, content)
    Future(())
  }
}
