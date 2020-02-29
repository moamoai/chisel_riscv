import sys

def _is_hex(val):
  try:
    int(val, 16)
    return True
  except ValueError, e:
    return False
def changeEndian(data): # data=12345678 -> 78563412
  return (data[6:8] + data[4:6] +  data[2:4] + data[0:2])

Inst_List = [] # [[ADDR, CODE, ASM] , ..]
Data_List = [] # [[ADDR, Data, ASM] , ..]

Start_ADDR = "0xffffffff80000000"
state = "inst"
for line in open("./data.dump"):
  line = line.rstrip()
  if(line == "Contents of section .data:"):
    state = "data"

  line = line.split()
  if((state == "inst") | (line == [])):
    continue

  # print line
  # print state
  if(_is_hex(line[0])):
    ADDR = int(line[0],16)
    for DATA in line[1:]:
      if(DATA[0]!="."):
        ADDR_str = "{0:08x}".format(ADDR)
        DATA = changeEndian(DATA)
        Data_List.append([ADDR_str, DATA, ".data"])
        ADDR += 4
# print(Data_List)

def write_txt(Inst_List, file_name):
  file = open(file_name, 'w')
  # print(Inst_List)
  for i in range(len(Inst_List)):
    Inst   = Inst_List[i]
    ADDR   = Inst[0]
    CODE   = Inst[1].replace("0x", "")
    if(_is_hex(CODE)):
      pass
    else:
      CODE = "0000"
    ASM    = Inst[2]
    inst = "{0} {1} // {2}\n".format(ADDR, CODE, ASM)
    file.writelines(inst)

  file.close()

write_txt(Data_List, './data.txt')

