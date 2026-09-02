mod exchange_app;

use exchange_app::{CreateAccountOptions, create_account};

fn main() {
    let opts = CreateAccountOptions {
        secret: "SSBSWUJ5247YPYN6CU7S4WC3CCTYSBUPUAHKAN2IHGTKI25KDEPGABQ7C".to_string(),
        starting_balance: "1000".to_string(),
    };

    match create_account(opts) {
        Ok(operation) => println!("Operação criada com sucesso: {:?}", operation),
        Err(e) => eprintln!("Erro: {:?}", e),
    }
}
