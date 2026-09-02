use soroban_sdk::xdr::{AccountId, CreateAccountOp};
use stellar_sdk::Keypair;
use soroban_sdk::xdr::Error as StellarError;
use thiserror::Error;
use std::str::FromStr;

#[derive(Clone)]
pub struct CreateAccountOptions {
    pub secret: String,
    pub destination: String,
    pub starting_balance: i64,
}

#[derive(Debug, Error)]
pub enum CreateAccountError {
    #[error("Chave secreta inválida: {0}")]
    InvalidSecretKey(String),
    #[error("Conta de destino inválida: {0}")]
    InvalidDestination(String),
    #[error("Erro geral: {0:?}")]
    GeneralError(StellarError),
}

impl From<StellarError> for CreateAccountError {
    fn from(e: StellarError) -> Self {
        CreateAccountError::GeneralError(e)
    }
}

// Função para criar uma conta
pub fn create_account(opts: CreateAccountOptions) -> Result<CreateAccountOp, CreateAccountError> {
    // Gerando a chave secreta
    let _secret_key = Keypair::from_secret_key(&opts.secret)
    .map_err(|_| CreateAccountError::InvalidSecretKey(opts.secret.clone()))?;

    // Obtendo a conta de destino a partir do ID
    let destination = AccountId::from_str(&opts.destination)
        .map_err(|_| CreateAccountError::InvalidDestination(opts.destination.clone()))?;

    // Criando a operação `CreateAccountOp`
    let operation = CreateAccountOp {
        destination,
        starting_balance: opts.starting_balance,
    };

    Ok(operation)
}

fn main() -> Result<(), CreateAccountError> {
    let opts = CreateAccountOptions {
        secret: "SBRF75NPABRHQWDVPJK3RSN2GDCDPHBGLLJ23JVTZAGJXGXXRMDR6Y5I".to_string(),
        destination: "GA66SXAPEFNWT7EZJZHZ5ZQ53YDFB2CYZ53JQCPUHBYMWAAYYLR2SRHJ".to_string(),
        starting_balance: 1000,
    };

    let operation = create_account(opts)?;
    println!("Operação criada com sucesso: {:?}", operation);

    Ok(())
}