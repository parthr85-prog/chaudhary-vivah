import pty
import os
import sys
import time

log_file = "/tmp/login_output.log"
url_file = "/tmp/auth_url.txt"

pid, master = pty.fork()

if pid == 0:
    # Child process
    os.execvp("npx", ["npx", "firebase-tools", "login", "--no-localhost"])
else:
    # Parent process
    with open(log_file, "w") as f:
        buffer = ""
        answered_y = False
        while True:
            try:
                data = os.read(master, 1024).decode('utf-8', errors='ignore')
                if not data:
                    break
                f.write(data)
                f.flush()
                buffer += data
                print(data, end="", flush=True)

                if "(Y/n)" in buffer and not answered_y:
                    os.write(master, b"y\n")
                    answered_y = True

                if "https://accounts.google.com" in buffer or "https://" in buffer:
                    # try to extract URL
                    lines = buffer.split('\n')
                    for line in lines:
                        if "http" in line:
                            with open(url_file, "w") as u_f:
                                u_f.write(line.strip())
            except OSError:
                break
