use std::collections::HashMap;
use std::fmt;
use std::hash::{Hash, Hasher};
use thiserror::Error;
use anyhow::Result;

#[derive(Clone, PartialEq, Eq)]
pub struct Exchange {
    token: String,  // Campo para armazenar o token.
    factory: String,  // Campo para armazenar a fábrica.
    server: String,  // Substituindo Network por String para simplificação.
}

impl Exchange {
    /// Cria uma nova instância de `Exchange`.
    pub fn new(token: String, factory: String, server: String) -> Self {
        Exchange { token, factory, server }
    }
}

impl Hash for Exchange {
    /// Implementação do método hash para a estrutura Exchange.
    fn hash<H: Hasher>(&self, state: &mut H) {
        self.token.hash(state);
        self.factory.hash(state);
        self.server.hash(state);
    }
}

impl fmt::Display for Exchange {
    /// Implementação do método fmt para a estrutura Exchange.
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(
            f,
            "Exchange {{ token: {}, factory: {}, server: {} }}",
            self.token, self.factory, self.server
        )
    }
}

pub struct DerivexFactory {
    server: String,  // Substituindo Network por String para simplificação.
    token_to_exchange: HashMap<String, Exchange>,  // Mapa de tokens para exchanges.
    exchange_to_token: HashMap<Exchange, String>,  // Mapa de exchanges para tokens.
}

impl DerivexFactory {
    /// Cria uma nova instância de `DerivexFactory`.
    pub fn new(server: String) -> Self {
        DerivexFactory {
            server,
            token_to_exchange: HashMap::new(),
            exchange_to_token: HashMap::new(),
        }
    }

    /// Cria uma nova exchange.
    pub fn create_new_exchange(&mut self, token_address: &str) -> Result<Exchange, DerivexError> {
        if self.token_to_exchange.contains_key(token_address) {
            return Err(DerivexError::DuplicateToken);
        }

        let exchange = Exchange::new(
            token_address.to_string(),
            "Factory".to_string(),
            self.server.clone(),
        );
        self.token_to_exchange
            .insert(token_address.to_string(), exchange.clone());
        self.exchange_to_token
            .insert(exchange.clone(), token_address.to_string());
        Ok(exchange)
    }

    /// Obtém uma exchange a partir de um endereço de token.
    pub fn get_exchange(&self, token_address: &str) -> Option<&Exchange> {
        self.token_to_exchange.get(token_address)
    }

    /// Obtém um token a partir de uma exchange.
    pub fn get_token(&self, exchange: &Exchange) -> Option<&String> {
        self.exchange_to_token.get(exchange)
    }

    /// Remove uma exchange a partir de um endereço de token.
    pub fn remove_exchange(&mut self, token_address: &str) {
        if let Some(exchange) = self.token_to_exchange.remove(token_address) {
            self.exchange_to_token.remove(&exchange);
        }
    }
}

#[derive(Debug, Error)]
pub enum DerivexError {
    #[error("Duplicate token address detected")]
    DuplicateToken,  // Erro para token duplicado.

    #[error("Invalid token address")]
    InvalidToken,  // Erro para token inválido.
}

#[tokio::main]
async fn main() -> Result<()> {
    let server = "https://horizon-testnet.stellar.org".to_string();
    let mut factory = DerivexFactory::new(server);

    match factory.create_new_exchange("TOKEN123") {
        Ok(exchange) => println!("Exchange criada: {}", exchange),
        Err(e) => eprintln!("Erro ao criar exchange: {}", e),
    }

    if let Some(exchange) = factory.get_exchange("TOKEN123") {
        println!("Exchange encontrada: {}", exchange);
    } else {
        println!("Exchange não encontrada.");
    }

    factory.remove_exchange("TOKEN123");
    if factory.get_exchange("TOKEN123").is_none() {
        println!("Exchange removida com sucesso.");
    }

    Ok(())
}
