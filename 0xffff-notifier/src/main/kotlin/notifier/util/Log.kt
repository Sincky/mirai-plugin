package notifier.util

class Log {

    companion object {
        private var delegate: LogDelegate? = null

        fun setDelegate(delegate: LogDelegate) {
            this.delegate = delegate
        }

        fun debug(msg: String?) {
            if (delegate != null) {
                delegate?.debug(msg)
            } else {
                println("debug: $msg")
            }
        }

        fun info(msg: String?) {
            if (delegate != null) {
                delegate?.info(msg)
            } else {
                println("info: $msg")
            }
        }

        fun error(msg: String?) {
            if (delegate != null) {
                delegate?.error(msg)
            } else {
                println("error: $msg")
            }
        }
    }

    interface LogDelegate {
        fun debug(msg: String?)
        fun info(msg: String?)
        fun error(msg: String?)
    }
}