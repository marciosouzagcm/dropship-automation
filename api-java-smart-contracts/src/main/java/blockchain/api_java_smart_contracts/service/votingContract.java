/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package blockchain.api_java_smart_contracts.service;

import java.math.BigInteger;

import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

// Esta classe parece ser uma versão simplificada ou um placeholder para interagir com o contrato de votação.
// Ela demonstra como chamadas remotas para funções do contrato poderiam ser estruturadas.
class votingContract {

    // Método estático que simula a chamada para a função 'autorizarEleitor' do contrato inteligente.
    // Recebe o endereço do eleitor (String) e um valor booleano indicando se ele deve ser autorizado.
    // Atualmente, lança uma UnsupportedOperationException, indicando que a implementação real não está presente aqui.
    static RemoteCall<TransactionReceipt> autorizarEleitor(String eleitorAddress, boolean b) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    // Método estático que simula a chamada para a função 'votar' do contrato inteligente.
    // Recebe o ID do candidato (BigInteger) para o qual o voto será registrado.
    // Atualmente, lança uma UnsupportedOperationException, indicando que a implementação real não está presente aqui.
    static RemoteCall<TransactionReceipt> votar(BigInteger valueOf) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}