import sys

Inst_List = [] # [[ADDR, CODE, ASM] , ..]

Start_ADDR = "0xffffffff80000000"
state = "init"
for line in open("../riscv_testpattern/hello.dump"):
  line = line.rstrip()
  line = line.split()
#   print state
  if(line == []):
    continue
  if((line[0][-1] == ":")&
     (line[0]     != "hello.elf:") ):
     ADDR = line[0].replace(":","")
     CODE = line[1]
     ASM    = " ".join(line[2:])
     Inst_List.append([ADDR, CODE, ASM])

file = open('./inst.txt', 'w')

print(Inst_List)
for i in range(len(Inst_List)):
  Inst   = Inst_List[i]
  ADDR   = Inst[0]
  CODE   = Inst[1]
  ASM    = Inst[2]
  inst = "{0} {1} // {2}\n".format(ADDR, CODE, ASM)
  file.writelines(inst)
  #print(inst)
file.close()