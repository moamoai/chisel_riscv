import sys


Inst_List = [] # [[ADDR, opcode, ExpectReg] , ..]
ExpectReg = [] # [[x0, value]

Start_ADDR = "0xffffffff80000000"
state = "init"
for line in open("../riscv_testpattern/run.log"):
  line = line.rstrip()
  line = line.split()
  print state
  print line
  if(state=="init"):
    ADDR   = line[2]
    if(ADDR == Start_ADDR):
      state = "get_inst"
    else:
      continue

  if(state=="get_inst"):
    if(line[0] == "core"):
      ADDR   = line[2].replace("ffffffff800", "")
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
    elif((op == "(0x0040006f)")   # j pc + 0x4
      |(op == "(0x0000006f)")): # j pc + 0x4
      state = "get_inst"
      continue
    elif(op=="(0x00532023)"): # Fin
      break

    reg    = line[4]
    expect = line[5]
    Inst_List.append([ADDR, opcode, [reg, expect], asm])
    state = "get_inst"

file = open('./inst.txt', 'w')

print(Inst_List)
for Inst in Inst_List:
  opcode = Inst[1].replace("0x","")
  reg    = Inst[2][0]
  expect = Inst[2][1].replace("0x","")
  asm    = Inst[3]
  inst = "{0} {1} {2} // {3}\n".format(opcode, reg, expect, asm)
  file.writelines(inst)
  #print(inst)

file.close()
