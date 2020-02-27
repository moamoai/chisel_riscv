package simple

import chisel3._
import chisel3.util._

class RF(DEBUG_INFO_EN:Int = 1) extends Module {
  val io = IO(new Bundle {
    var if_RFtoEX = new IF_RFtoEX
    var if_WBtoRF = Flipped(new IF_WBtoRF)
    var if_IDtoRF = Flipped(new IF_IDtoRF)
    // Debug Port
    // var info_rf = if(DEBUG_INFO_EN==1) Output(Vec(4, UInt(32.W))) else None
    var info_rf = Output(Vec(32, UInt(32.W)))
  })
  val r_RegFile = RegInit(VecInit(Seq.fill(32)(0.U(32.W))))

  io.info_rf := r_RegFile
//  val info_rf = Wire(Vec(32, UInt(32.W)))
//  if(DEBUG_INFO_EN==1){
//    info_rf := r_RegFile
//    io.info_rf := info_rf
//  }

  var rd     = io.if_IDtoRF.rd
  var rs1    = io.if_IDtoRF.rs1
  var rs2    = io.if_IDtoRF.rs2

  var wvalid = io.if_WBtoRF.valid
  var wdata  = io.if_WBtoRF.wdata
  var w_rd   = io.if_WBtoRF.rd

  io.if_RFtoEX.d_rd  := r_RegFile(rd )
  io.if_RFtoEX.d_rs1 := r_RegFile(rs1)
  io.if_RFtoEX.d_rs2 := r_RegFile(rs2)

  when(wvalid===1.U){
    when(w_rd != 0.U){
      r_RegFile(w_rd) := wdata
    }
  }
}