package simple

import chisel3._
import chisel3.util._

class ID extends Module {
  val io = IO(new Bundle {
    val if_IFtoID      = Flipped(new IF_IFtoID)
    var if_IDtoEX      = new IF_IDtoEX
    var if_IDtoRF      = new IF_IDtoRF
    val illigal_op     = Output (UInt(1.W))
  })

  val inst_code = Wire(UInt(32.W))
  inst_code := io.if_IFtoID.opcode

  var opcode = inst_code(6,0)
  var rd = inst_code(11,7)
  var func3 = inst_code(14,12)
  var rs1 = inst_code(19,15)
  var rs2 = inst_code(24,20)
  var func7 = inst_code(31,25)
  var imm_I = inst_code(31,20)
  // var imm_S = inst_code(7,7)
  var imm_U = (inst_code(31,12) << 12)
  // var imm_J = inst_code(31,31)
  var shamt = inst_code(24,20)

  // imm sel
  val imm = Wire(UInt(32.W))
  imm := 0.U
  // opcode
  val illigal_op   = Wire(UInt(1.W))
  val lui_valid    = Wire(Bool())
  val load_valid   = Wire(Bool())
  val op_imm_valid = Wire(Bool())
  val op_valid     = Wire(Bool())
  val store_valid  = Wire(Bool())
  illigal_op   := 0.U
  lui_valid    := 0.U
  load_valid   := 0.U
  op_imm_valid := 0.U
  op_valid     := 0.U
  store_valid  := 0.U
  
  val alu_func  = Wire(UInt(6.W))
  val ldst_func = Wire(UInt(6.W))
  alu_func  := 0.U
  ldst_func := (load_valid << 5) + (store_valid << 4) + func3


  // Decorder
  when(io.if_IFtoID.valid === 1.U){
    when(opcode===0x03.U){       //  LOAD/I-type
      load_valid := 1.U
      imm := imm_I
    }.elsewhen(opcode===0x13.U){ // OP-IMM/I-type
      op_imm_valid := 1.U
      imm := imm_I
      alu_func := func3
    }.elsewhen(opcode===0x33.U){ // OP/R-type
      op_valid := 1.U
      imm := 0.U
      alu_func := func3
    }.elsewhen(opcode===0x23.U){ // STORE
      store_valid := 1.U
      imm := (inst_code(31,25)<<5) + inst_code(4,0)
    }.elsewhen(opcode===0x37.U){ // LUI U-type
      alu_func := OBJ_ALU_FUNC.SEL_B
      lui_valid := 1.U
      imm := imm_U
    }.otherwise{
      illigal_op := 1.U
    }
  }
  // assert(illigal_op === 0x0.U, "[NG]Illigal OP!!")

  // Output
  io.if_IDtoEX.alu_func  := alu_func
  io.if_IDtoEX.ldst_func := ldst_func
  io.if_IDtoEX.imm       := imm
  io.if_IDtoEX.imm_sel   := op_imm_valid | lui_valid
  io.if_IDtoEX.rd        := rd
  io.if_IDtoEX.valid     := op_valid | op_imm_valid | 
                            load_valid | store_valid | lui_valid
  io.if_IDtoRF.rd        := rd
  io.if_IDtoRF.rs1       := rs1
  io.if_IDtoRF.rs2       := rs2

  io.illigal_op := illigal_op
}
