import sys


Inst_List = [] # [[ADDR, opcode, ExpectReg] , ..]
ExpectReg = [] # [[x0, value]

Start_ADDR = "0xffffffff80000000"
state = "init"
for line in open("../riscv_testpattern/run.log"):
  line = line.rstrip()
  line = line.split()
  # print state
  # print line
  if(state=="init"):
    ADDR   = line[2]
    if(ADDR == Start_ADDR):
      state = "get_inst"
    else:
      continue
  if(state=="get_inst"):
    if(line[0] == "core"):
      ADDR   = line[2].replace("ffffffff", "")
      opcode = line[3].replace("(", "").replace(")", "")
      asm    = " ".join(line[4:])
      state = "get_expect"
    else:
      print("No expect register. jump loop or else")
      break 
  elif(state=="get_expect"):
    op    = line[2]
    if(op == "(0x00000013)"):   # NOP
      line.append("0")
      line.append("0")
      line.append("0x00000000")
    elif((op == "(0x0040006f)") # j pc + 0x4
      |(op == "(0x00008067)")   #  ret
      |(op == "(0x0000006f)")   # j pc + 0x0
      ): 
      # state = "get_inst"
      # continue
      line.append("0")
      line.append("0")
      line.append("0x00000000")
    elif(op=="(0x00532023)"): # Fin
      break

    regx = line[3]
    if (op[9:12] == "23)"):# sw(0x13)
      reg    = "00"
      expect = "0x00000000"
    elif(len(regx)==3): # x10,x11..
      reg    = regx[1:3]
      expect = line[4]
    else: # x 1, 
      reg    = "0"+line[4]
      expect = line[5]
    Inst_List.append([ADDR, opcode, [reg, expect], asm])
    state = "get_inst"

file = open('./expect.txt', 'w')

# print(Inst_List)
for i in range(len(Inst_List)):
  Inst = Inst_List[i]
  opcode    = Inst[1].replace("0x","")
  reg       = Inst[2][0]
  expect    = Inst[2][1].replace("0x","")
  asm       = Inst[3]
  ADDR      = Inst[0].replace("0x", "")

  if(i!=len(Inst_List)-1):
    Inst_Next = Inst_List[i+1]
    NEXT_ADDR = Inst_Next[0].replace("0x", "")
  else:
    NEXT_ADDR = 0
  inst = "{0} {1} {2} {3} {4} // {5}\n".format(opcode, reg, expect, ADDR, NEXT_ADDR, asm)
  file.writelines(inst)
  #print(inst)

file.close()
