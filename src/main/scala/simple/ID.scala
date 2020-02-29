package simple

import chisel3._
import chisel3.util._

class ID extends Module {
  val io = IO(new Bundle {
    val if_IDtoIF      = new IF_IDtoIF
    var if_IDtoEX      = new IF_IDtoEX
    var if_IDtoRF      = new IF_IDtoRF
    val if_IFtoID      = Flipped(new IF_IFtoID)
    val if_RFtoID      = Flipped(new IF_RFtoID)
    val illigal_op     = Output (UInt(1.W))
  })

  val inst_code = Wire(UInt(32.W))
  inst_code := io.if_IFtoID.opcode

  var opcode = inst_code(6,0)
  val rd = Wire(UInt(5.W))
  rd := 0.U
  var func3 = inst_code(14,12)
  var rs1 = inst_code(19,15)
  var rs2 = inst_code(24,20)
  var func7 = inst_code(31,25)
  // // var imm_I = inst_code(31,20)
  // val imm_I_SInt = Wire(SInt(12.W))
  // imm_I_SInt := inst_code(31,20).asSInt
  val imm_I  = Wire(UInt(32.W))
  when(inst_code(31)===0.U){ // +
    imm_I      := inst_code(31,20)
  }.otherwise{
    imm_I      := (0xFFFFF000L.U | inst_code(31,20))
  }
  val imm_S  = Wire(UInt(32.W))
  when(inst_code(31)===0.U){ // +
    imm_S := (inst_code(31,25)<<5) + inst_code(11,7)
  }.otherwise{
    imm_S := (0xFFFFF000L.U | (inst_code(31,25)<<5) + inst_code(11,7))
  }

  var imm_U = (inst_code(31,12) << 12)
  // var imm_J = inst_code(31,31)
  var shamt = inst_code(24,20)

  var imm_J = (inst_code(31)   <<20) + // [20]
              (inst_code(19,12)<<12) + // [19:12]
              (inst_code(20)   <<11) + // [11]
              (inst_code(30,21)<<1 )   // [10:1]

  val imm_B    = Wire(UInt(32.W))
  val imm_B_12 = Wire(UInt(12.W))
  // imm_B := (inst_code(31)    <<12) + //[12]
  imm_B_12 := (inst_code(7)     <<11) + //[11]
              (inst_code(30,25) <<5 ) + //[10:5]
              (inst_code(11,8)  <<1 )   //[4:1]
  imm_B := Cat(Fill(20, inst_code(31)), imm_B_12)

  // imm sel
  val imm = Wire(UInt(32.W))
  imm := 0.U
  // opcode
  val illigal_op   = Wire(UInt(1.W))
  val lui_valid    = Wire(Bool())
  val auipc_valid  = Wire(Bool())
  val load_valid   = Wire(Bool())
  val op_imm_valid = Wire(Bool())
  val op_valid     = Wire(Bool())
  val store_valid  = Wire(Bool())
  val jal_valid    = Wire(Bool())
  val jalr_valid   = Wire(Bool())
  val bra_valid    = Wire(Bool())
  val csrw_valid   = Wire(Bool())
  val csrr_valid   = Wire(Bool())
  val fence_valid  = Wire(Bool())
  illigal_op   := 0.U
  lui_valid    := 0.U
  auipc_valid  := 0.U
  load_valid   := 0.U
  op_imm_valid := 0.U
  op_valid     := 0.U
  store_valid  := 0.U
  jal_valid    := 0.U
  jalr_valid   := 0.U
  bra_valid    := 0.U
  csrw_valid   := 0.U
  csrr_valid   := 0.U
  fence_valid  := 0.U
  
  val alu_func  = Wire(UInt(6.W))
  val ldst_func = Wire(UInt(6.W))
  alu_func  := 0.U
  ldst_func := func3

  val PC = Wire(UInt(32.W))
  PC := io.if_IFtoID.PC
  val d_rs1 = Wire(UInt(32.W))
  val d_rs2 = Wire(UInt(32.W))
  d_rs1 := io.if_RFtoID.d_rs1
  d_rs2 := io.if_RFtoID.d_rs2
  when((csrw_valid === 1.U)|| (bra_valid === 1.U) || (fence_valid === 1.U)){
    rd := 0.U // Nop for cswr.
  }.otherwise{
    rd := inst_code(11,7)
  }

  // Decorder
  when(io.if_IFtoID.valid === 1.U){
    when(opcode===0x03.U){       //  LOAD/I-type
      load_valid := 1.U
      imm := imm_I
    }.elsewhen(opcode===0x13.U){ // OP-IMM/I-type
      op_imm_valid := 1.U
      imm := imm_I
      // alu_func := (inst_code(30)<<3) | func3
      alu_func := ((inst_code(31,25)==="b0100000".U)<<3) | func3
    }.elsewhen(opcode===0x33.U){ // OP/R-type
      op_valid := 1.U
      imm := 0.U
      alu_func := ((inst_code(31,25)==="b0100000".U)<<3) | func3
    }.elsewhen(opcode===0x23.U){ // STORE
      store_valid := 1.U
      imm := imm_S
    }.elsewhen(opcode===0x37.U){ // LUI U-type
      alu_func := OBJ_ALU_FUNC.SEL_B
      lui_valid := 1.U
      imm := imm_U
    }.elsewhen(opcode===0x17.U){ // AUIPC U-type
      alu_func    := OBJ_ALU_FUNC.SEL_B
      auipc_valid := 1.U
      imm := PC + imm_U
    }.elsewhen(opcode===0x6F.U){ // JAL J-type
      alu_func := OBJ_ALU_FUNC.SEL_B
      jal_valid := 1.U
      imm := PC + 0x04.U
    }.elsewhen(opcode===0x67.U){ // JALR I-type
      alu_func := OBJ_ALU_FUNC.SEL_B
      imm := PC + 0x04.U
      jalr_valid := 1.U
    }.elsewhen(opcode===0x63.U){ // BRA B-type
      bra_valid := 1.U
    }.elsewhen(opcode===0x73.U){ // CSR
      when(func3==="b001".U){ // CSRRW
        csrw_valid := 1.U
      }.otherwise{            // CSRRR
        csrr_valid := 1.U
      }
    }.elsewhen(opcode===0x0F.U){ // FENCE
      fence_valid := 1.U
    }.otherwise{  // Illegal
      illigal_op := 1.U
      alu_func := OBJ_ALU_FUNC.SEL_B
      imm      := 0.U // tmp(csr read data)
    }
  }
  // assert(illigal_op === 0x0.U, "[NG]Illigal OP!!")

  // jump 
  val jump_addr      = Wire(UInt(32.W))
  val jump_valid     = Wire(UInt(1.W))
  val bra_valid_true = Wire(UInt(1.W))
  jump_addr      := 0.U
  bra_valid_true := 0.U
  jump_valid     := jal_valid | jalr_valid
  when(jal_valid===1.U){
    jump_addr  := PC + imm_J
  }.elsewhen(jalr_valid===1.U){
    jump_addr  := d_rs1 // + imm_I
  }.elsewhen(bra_valid===1.U){
    jump_addr  := PC + imm_B
    when(((func3===0.U) && (d_rs1 ===d_rs2))|| // BEQ
         ((func3===1.U) && (d_rs1 != d_rs2))|| // BNE
         ((func3===4.U) && (d_rs1.asSInt <  d_rs2.asSInt))|| // BLT
         ((func3===5.U) && (d_rs1.asSInt >= d_rs2.asSInt))|| // BGE
         ((func3===6.U) && (d_rs1  < d_rs2))|| // BLTU
         ((func3===7.U) && (d_rs1 >= d_rs2)))  // BGEU
    {
      bra_valid_true := 1.U
    }
  }
  io.if_IDtoIF.jump_addr  := jump_addr
  io.if_IDtoIF.jump_valid := jump_valid | bra_valid_true

  // Output
  io.if_IDtoEX.alu_func    := alu_func
  io.if_IDtoEX.ldst_func   := ldst_func
  io.if_IDtoEX.imm         := imm
  io.if_IDtoEX.imm_sel     := op_imm_valid | lui_valid | 
                              jal_valid | jalr_valid |
                              auipc_valid
  io.if_IDtoEX.rd          := rd
  io.if_IDtoEX.alu_valid   := op_valid   | op_imm_valid | 
                              lui_valid  | auipc_valid |
                              jal_valid  | jalr_valid  |
                              csrw_valid | csrr_valid | // tmp. 
                              bra_valid  | fence_valid
  io.if_IDtoEX.load_valid  := load_valid
  io.if_IDtoEX.store_valid := store_valid
  io.if_IDtoRF.rd          := rd
  io.if_IDtoRF.rs1         := rs1
  io.if_IDtoRF.rs2         := rs2

  io.illigal_op := illigal_op
}
