#!/bin/bash -e

mkdir -p logs

# LOG_DIR="../riscv_testpattern/logs/"
# PAT_DIR="../01_comp/riscv-tests/isa/"

grep -v "#" test_list_all > test_list

echo "" > result.log
for test in `cat test_list`
do
  echo "# ${test}"
  rm -f run_spike.log
  rm -f pattern.dump
  ln -s ${LOG_DIR}/run_${test}.log ./run_spike.log
  ln -s ${PAT_DIR}/${test}.dump    ./pattern.dump
  python tools/gen_expect.py > gen_expect.log
  python tools/gen_inst.py   > gen_inst.log 
 
  make riscv-test | tee logs/run_${test}.log
  RESULT=`grep -e "OK" -e "NG" -e "ERROR" logs/run_${test}.log`
  TIME=`grep -e "Total time" logs/run_${test}.log`
  echo "${test}: ${RESULT} ${TIME}"  >> result.log
done
