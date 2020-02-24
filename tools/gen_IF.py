
import sys

state = ""

IFName_List = [] # [[IFName,SignalList] , ..]
Signal_List = [] # [[valid,width],[ready,1],...]
for line in open("./interface.txt"):
  line = line.rstrip()
  line = line.split("	")
  if(line[0]!=""):
    if(state == "GetIFName"): 
        IFName_List.append([IFName, Signal_List])
        Signal_List = []
    IFName = line[0]
    Signal = line[1]
    Width  = line[2]
    Signal_List.append([Signal, Width])
    state = "GetIFName"
  else: # Signal Only
    Signal = line[1]
    Width  = line[2]
    Signal_List.append([Signal, Width])

print(IFName_List)    

for line in (IFName_List):
  IFName  = line[0]
  Signals = line[1]
  print("class {0:10s} extends Bundle {{".format(IFName))
  for signal in Signals:
    print("  val {0:10s} = Output(UInt({1:>2s}.W))".format(signal[0], signal[1]))
#    val AWPROT = Output(UInt(1.W))
#    val AWVALID = Output(UInt(1.W))
#    val AWREADY = Input (UInt(1.W))
  print("}")