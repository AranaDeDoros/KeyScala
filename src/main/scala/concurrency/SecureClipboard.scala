package org.aranadedoros
package concurrency

import cats.effect.IO
import cats.effect.kernel.Outcome

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

object SecureClipboard:

  private val ec: ExecutionContext =
    ExecutionContext.global

  def copyTemporarily(content: String, duration: FiniteDuration): IO[Unit] =
    def clearClipboard(chars: Array[Char]): IO[Unit] =
      IO {
        val clipboard = Toolkit.getDefaultToolkit.getSystemClipboard
        clipboard.setContents(new StringSelection(""), null)
        java.util.Arrays.fill(chars, '\u0000')
      }
    for
      chars     <- IO(content.toCharArray)
      clipboard <- IO(Toolkit.getDefaultToolkit.getSystemClipboard)
      _ <- IO {
        val selection = new StringSelection(new String(chars))
        clipboard.setContents(selection, null)
      }
      _ <- IO.sleep(duration)
        .guaranteeCase {
          case Outcome.Succeeded(_) =>
            clearClipboard(chars)

          case Outcome.Canceled() =>
            IO.println("clipboard canceled") >>
              clearClipboard(chars)

          case Outcome.Errored(e) =>
            IO.println(s"an error occurred: ${e.getMessage}") >>
              clearClipboard(chars)
        }
    yield ()
