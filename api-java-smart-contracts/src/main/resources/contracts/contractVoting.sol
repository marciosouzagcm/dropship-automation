// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

/// @title Contrato de Votação
/// @author marciosouzagcm@gmail.com
/// @dev Este contrato implementa um sistema de votação simples onde um administrador pode adicionar candidatos e autorizar eleitores a votar em um deles.
contract Voting {
    /// @notice Estrutura para armazenar informações de cada candidato.
    struct Candidato {
        uint256 id;             ///< ID único do candidato.
        string nome;            ///< Nome do candidato.
        uint256 contagemDeVotos; ///< Número de votos recebidos pelo candidato.
    }

    /// @notice Estrutura para armazenar informações de cada eleitor.
    struct Eleitor {
        bool autorizado; ///< Indica se o eleitor foi autorizado a votar pelo administrador.
        bool votou;     ///< Indica se o eleitor já votou.
        uint256 voto;   ///< ID do candidato em quem o eleitor votou.
    }

    /// @notice Mapeia um ID de candidato para a sua estrutura de informações `Candidato`.
    /// @dev Permite acessar as informações de um candidato usando seu ID.
    mapping(uint256 => Candidato) public candidatos;

    /// @notice Mapeia o endereço de um eleitor para a sua estrutura de informações `Eleitor`.
    /// @dev Permite verificar o status de um eleitor (autorizado, votou, em quem votou) usando seu endereço.
    mapping(address => Eleitor) public eleitores;

    /// @notice Contador para rastrear o número total de candidatos adicionados.
    /// @dev O ID de um novo candidato é geralmente definido como o valor atual deste contador antes de ser incrementado.
    uint256 public contadorDeCandidatos;

    /// @notice Endereço da conta que tem permissões de administrador.
    /// @dev Apenas o administrador pode adicionar candidatos e autorizar eleitores.
    address public administrador;

    /// @notice Evento emitido quando um novo candidato é adicionado ao sistema.
    event CandidatoAdicionado(uint256 id, string nome);

    /// @notice Evento emitido quando um voto é registrado com sucesso.
    event VotoRegistrado(uint256 id, string nome, address eleitor);

    /// @notice Evento emitido quando ocorre uma tentativa de voto inválida.
    event VotoFalhou(uint256 idCandidato, address eleitor, string motivo);

    /// @notice Evento emitido quando um eleitor é autorizado ou desautorizado.
    event EleitorAutorizado(address eleitor, bool autorizado);

    /// @notice Modificador que restringe a execução de uma função apenas ao administrador.
    modifier apenasAdministrador() {
        require(msg.sender == administrador, "Apenas o administrador pode executar esta funcao.");
        _; // Permite que a execução da função modificada continue.
    }

    /// @notice Construtor do contrato. Define o endereço da conta que implantou o contrato como o administrador inicial.
    constructor() {
        administrador = msg.sender;
    }

    /// @notice Função para adicionar um novo candidato ao sistema. Apenas o administrador pode chamar esta função.
    /// @param _nome O nome do candidato a ser adicionado.
    function adicionarCandidato(string memory _nome) public apenasAdministrador {
        require(bytes(_nome).length > 0, "Nome do candidato nao pode estar vazio.");

        // Incrementa o contador de candidatos antes de atribuir o ID ao novo candidato.
        contadorDeCandidatos++;
        // Cria uma nova estrutura Candidato e a armazena no mapeamento `candidatos` usando o novo `contadorDeCandidatos` como ID.
        candidatos[contadorDeCandidatos] = Candidato(contadorDeCandidatos, _nome, 0);

        // Emite um evento para registrar a adição do candidato, incluindo seu ID e nome.
        emit CandidatoAdicionado(contadorDeCandidatos, _nome);
    }

    /// @notice Função para autorizar ou desautorizar um eleitor a votar. Apenas o administrador pode chamar esta função.
    /// @param _eleitor O endereço do eleitor a ser autorizado ou desautorizado.
    /// @param _autorizado Um booleano indicando se o eleitor deve ser autorizado (true) ou desautorizado (false).
    function autorizarEleitor(address _eleitor, bool _autorizado) public apenasAdministrador {
        require(_eleitor != address(0), "Endereco do eleitor invalido.");
        // Atualiza o status de autorização do eleitor no mapeamento `eleitores`.
        eleitores[_eleitor].autorizado = _autorizado;
        // Emite um evento para registrar a autorização (ou desautorização) do eleitor.
        emit EleitorAutorizado(_eleitor, _autorizado);
    }

    /// @notice Função para obter as informações de um candidato específico.
    /// @param _id O ID do candidato a ser consultado.
    /// @return (uint256, string memory, uint256) Retorna o ID, nome e contagem de votos do candidato.
    function obterCandidato(uint256 _id) public view returns (uint256, string memory, uint256) {
        require(_id > 0 && _id <= contadorDeCandidatos, "Candidato invalido.");
        // Recupera a estrutura Candidato do mapeamento `candidatos` usando o ID fornecido.
        Candidato memory candidato = candidatos[_id];
        return (candidato.id, candidato.nome, candidato.contagemDeVotos);
    }

    /// @notice Função para um eleitor votar em um candidato.
    /// @param _id O ID do candidato em quem o eleitor deseja votar.
    function votar(uint256 _id) public {
        // Recupera a estrutura Eleitor do remetente da mensagem (o eleitor).
        Eleitor storage eleitor = eleitores[msg.sender];

        // Verifica se o eleitor está autorizado a votar.
        if (!eleitor.autorizado) {
            emit VotoFalhou(_id, msg.sender, "Eleitor nao autorizado.");
            revert("Eleitor nao autorizado.");
        }

        // Verifica se o eleitor já votou.
        if (eleitor.votou) {
            emit VotoFalhou(_id, msg.sender, "Eleitor ja votou.");
            revert("Eleitor ja votou.");
        }

        // Verifica se o ID do candidato é válido.
        if (_id == 0 || _id > contadorDeCandidatos) {
            emit VotoFalhou(_id, msg.sender, "Candidato invalido.");
            revert("Candidato invalido.");
        }

        // Marca o eleitor como tendo votado e registra o ID do candidato em quem ele votou.
        eleitor.votou = true;
        eleitor.voto = _id;

        // Recupera a estrutura Candidato do mapeamento `candidatos` usando o ID fornecido e incrementa a contagem de votos.
        Candidato storage candidato = candidatos[_id];
        candidato.contagemDeVotos++;

        // Emite um evento para registrar o voto, incluindo o ID do candidato, seu nome e o endereço do eleitor.
        emit VotoRegistrado(_id, candidato.nome, msg.sender);
    }

    /// @notice Função para verificar se um determinado endereço já votou.
    /// @param _eleitor O endereço do eleitor a ser verificado.
    /// @return (bool) Retorna true se o eleitor já votou, false caso contrário.
    function verificarSeVotou(address _eleitor) public view returns (bool) {
        require(_eleitor != address(0), "Endereco do eleitor invalido.");
        return eleitores[_eleitor].votou;
    }

    /// @notice Função para obter o número total de candidatos registrados no sistema.
    /// @return (uint256) Retorna o número total de candidatos.
    function obterTotalDeCandidatos() public view returns (uint256) {
        return contadorDeCandidatos;
   }
}