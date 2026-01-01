package org.aranadedoros.keyscala
package concurrency

import cats.syntax.all._
import cats.effect.{IO, Resource}
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

object SecureClipboard:

  def resource(content: String): Resource[IO, Unit] =
    Resource.make {
      IO.blocking {
        val chars     = content.toCharArray
        val clipboard = Toolkit.getDefaultToolkit.getSystemClipboard
        val selection = new StringSelection(new String(chars))
        clipboard.setContents(selection, null)
        chars
      }
    } { chars =>
      IO.blocking {
        val clipboard = Toolkit.getDefaultToolkit.getSystemClipboard
        clipboard.setContents(new StringSelection(""), null)
        java.util.Arrays.fill(chars, '\u0000')
      }
    }.void
