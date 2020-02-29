import sys

def _is_hex(val):
  try:
    int(val, 16)
    return True
  except ValueError, e:
    return False

Inst_List = [] # [[ADDR, CODE, ASM] , ..]
Data_List = [] # [[ADDR, Data, ASM] , ..]

Start_ADDR = "0xffffffff80000000"
state = "inst"
for line in open("./pattern.dump"):
  line = line.rstrip()
  if(line == "Disassembly of section .data:"):
    state = "data"

  line = line.split()
#   print state
  if(line == []):
    continue
  if((line[0][-1] == ":")&
     _is_hex(line[0][0:8])):
    #(line[0]     != "hello.elf:") ):
    ADDR = line[0].replace(":","")
    CODE = line[1]
    ASM    = " ".join(line[2:])
    if(state=="inst"):
      Inst_List.append([ADDR, CODE, ASM])
    elif(state=="data"):
      Data_List.append([ADDR, CODE, ASM])

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

write_txt(Inst_List, './inst.txt')
write_txt(Data_List, './data.txt')

