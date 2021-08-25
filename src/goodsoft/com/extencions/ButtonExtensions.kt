package goodsoft.com.extencions

import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.JButton

fun JButton?.setOnClickListener(listener: () -> Unit){
    this?.addMouseListener(object: MouseListener {
        override fun mouseReleased(e: MouseEvent?) {
        }

        override fun mouseEntered(e: MouseEvent?) {
        }

        override fun mouseClicked(e: MouseEvent?) {
            return listener.invoke()
        }

        override fun mouseExited(e: MouseEvent?) {
        }

        override fun mousePressed(e: MouseEvent?) {
        }
    })
}

