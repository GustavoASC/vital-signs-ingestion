import psutil

while True:
    print(psutil.cpu_percent(interval=1))
