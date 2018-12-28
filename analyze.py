import sys
from matplotlib import pyplot as plt
import numpy

class DataChank:
    def __init__(self):
        self.time_passed = None
        self.users = None
        self.accounts = None
        self.yangluo_money = None
        self.called = None
        self.last = None
        self.avg = None



if __name__ == "__main__":
    chanks = []
    currentChank = None
    filename=sys.argv[1]
    time = False
    if len(sys.argv)>2:
        time=sys.argv[2]=="-t"
    with open(filename, 'r') as log_file:
        for line in log_file:
            if line == "\n" or line == "":
                if currentChank!=None:
                    chanks.append(currentChank)
                currentChank = DataChank()
                # break
            elif "time passed:" in line:
                currentChank.time_passed = line.split(' ')[2]
            elif "users:" in line:
                currentChank.users = line.split(' ')[1]
            elif "accounts:" in line:
                currentChank.accounts = line.split(' ')[1]
            elif "yangluo money:" in line:
                currentChank.yangluo_money = line.split(' ')[2]
            elif "play_scenario_1" in line:
                currentChank.called = line.split(' ')[3]
            elif "Execution time last:" in line:
                currentChank.last = line.split(' ')[3][:-1]
                currentChank.avg = line.split(' ')[5]
    fig = plt.figure()
    ax = fig.gca()
    ax.set_xticks(numpy.arange(0, 2000, 100))
    ax.set_yticks(numpy.arange(0, 10., 0.5))
    x = [ch.time_passed for ch in  chanks] if time else [ch.called for ch in  chanks]
    y1 = [ch.last for ch in  chanks]
    y2 = [ch.avg for ch in  chanks]
    plt.scatter(x, y1)
    plt.scatter(x, y2)
    plt.grid()
    plt.show()
