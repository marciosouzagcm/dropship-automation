use stellar_base::crypto::KeyPair;
use stellar_base::xdr::{CreateAccountOp, Int64 as StellarInt64, AccountId};
use stellar_base::error::Error as StellarError;
use thiserror::Error;
use base64;

// Estrutura para opções de criação de conta
#[derive(Clone)]
pub struct CreateAccountOptions {
    pub secret: String,
    pub destination: String,
    pub starting_balance: String,
}

// Enum de erros personalizados
#[derive(Debug, Error)]
pub enum CreateAccountError {
    #[error("Chave secreta inválida: {0}")]
    InvalidSecretKey(String),
    #[error("Conta de destino inválida: {0}")]
    InvalidDestination(String),
    #[error("Saldo inicial inválido")]
    InvalidStartingBalance,
    #[error("Erro geral: {0}")]
    GeneralError(String),
}

// Conversão de erro de Stellar para CreateAccountError
impl From<StellarError> for CreateAccountError {
    fn from(e: StellarError) -> Self {
        CreateAccountError::GeneralError(e.to_string())
    }
}

// Função para criar uma conta
pub fn create_account(opts: CreateAccountOptions) -> Result<CreateAccountOp, CreateAccountError> {
    // Validando e convertendo o saldo inicial
    let starting_balance: i64 = opts
        .starting_balance
        .parse()
        .map_err(|_| CreateAccountError::InvalidStartingBalance)?;

    // Convertendo o destino para AccountId
    let destination = match AccountId::from(opts.destination.clone()) {
        Ok(account_id) => account_id,
        Err(_) => return Err(CreateAccountError::InvalidDestination("Formato do destino inválido".to_string())),
    };

    // Criando o objeto CreateAccountOp
    Ok(CreateAccountOp {
        destination,
        starting_balance: StellarInt64 { value: starting_balance },
    })
}

// Função principal
fn main() -> Result<(), CreateAccountError> {
    // Configuração inicial
    let opts = CreateAccountOptions {
        secret: "SB3XQ23EVRPIYFYANP7LKKAA7RTEJFOETLAVZUKAEXAMPLE".to_string(),
        destination: "GCO6RS2XBNLLO4QK2HZMY4NTX2R7PVSCZIHUV4DQEXAMPLE".to_string(),
        starting_balance: "1000".to_string(),
    };

    // Gerando a chave pública a partir da chave secreta
    let keypair = KeyPair::from_secret_seed(&opts.secret).map_err(|_| {
        CreateAccountError::InvalidSecretKey("Chave secreta inválida ou malformada".to_string())
    })?;
    let public_key = keypair.public_key();

    // Criando a operação de conta
    let operation = create_account(opts)?;

    // Exibindo resultados
    println!("Operação criada com sucesso: {:?}", operation);
    println!("Chave Pública (Hexadecimal): {:?}", format!("{:?}", public_key));
    println!("Chave Pública (Base64): {:?}", base64::encode_config(public_key.as_bytes(), base64::STANDARD));

    Ok(())
}