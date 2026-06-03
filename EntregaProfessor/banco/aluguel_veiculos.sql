-- ============================================================
-- PROJETO: SISTEMA DE ALUGUEL DE VEICULOS
-- ============================================================

DROP DATABASE IF EXISTS aluguel_veiculos;
CREATE DATABASE aluguel_veiculos CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE aluguel_veiculos;

-- ------------------------------------------------------------
-- TABELA: usuario
-- ------------------------------------------------------------
CREATE TABLE usuario (
    id       INT          AUTO_INCREMENT PRIMARY KEY,
    nome     VARCHAR(100) NOT NULL,
    cargo    VARCHAR(50),
    login    VARCHAR(50)  NOT NULL UNIQUE,
    senha    VARCHAR(100) NOT NULL,
    email    VARCHAR(100)
);

-- ------------------------------------------------------------
-- TABELA: cliente
-- ------------------------------------------------------------
CREATE TABLE cliente (
    cpf         VARCHAR(14)  PRIMARY KEY,
    nomeCliente VARCHAR(100) NOT NULL,
    endereco    VARCHAR(200),
    uf          CHAR(2),
    telefone    VARCHAR(20),
    email       VARCHAR(100)
);

-- ------------------------------------------------------------
-- TABELA: veiculo
-- ------------------------------------------------------------
CREATE TABLE veiculo (
    numero      VARCHAR(20) PRIMARY KEY,
    placa       VARCHAR(10) NOT NULL UNIQUE,
    fabricante  VARCHAR(50),
    modelo      VARCHAR(50),
    anoModelo   INT,
    qtdPortas   INT,
    acessorios  TEXT
);

-- ------------------------------------------------------------
-- TABELA: aluguel
-- ------------------------------------------------------------
CREATE TABLE aluguel (
    idaluguel       INT           AUTO_INCREMENT PRIMARY KEY,
    numero_veiculo  VARCHAR(20)   NOT NULL,
    cpf_cliente     VARCHAR(14)   NOT NULL,
    dataAluguel     DATE,
    dataentrega     DATE,
    entregue        CHAR(1)       DEFAULT 'N',
    observacao      TEXT,
    valorPago       DECIMAL(10,2),
    CONSTRAINT fk_veiculo FOREIGN KEY (numero_veiculo) REFERENCES veiculo(numero),
    CONSTRAINT fk_cliente FOREIGN KEY (cpf_cliente)    REFERENCES cliente(cpf)
);

-- ============================================================
-- INSERTS DE TESTE
-- ============================================================

-- Usuario
INSERT INTO usuario (nome, cargo, login, senha, email) VALUES
('Administrador', 'Gerente', 'admin', 'admin123', 'admin@aluguelveiculos.com');

-- Clientes
INSERT INTO cliente (cpf, nomeCliente, endereco, uf, telefone, email) VALUES
('123.456.789-00', 'Carlos Eduardo Silva',   'Rua das Flores, 123, Bairro Centro',      'SP', '(11) 98765-4321', 'carlos.silva@email.com'),
('987.654.321-00', 'Mariana Costa Oliveira', 'Av. Paulista, 456, Apto 12',              'SP', '(11) 91234-5678', 'mariana.oliveira@email.com'),
('456.123.789-00', 'Roberto Alves Ferreira', 'Rua XV de Novembro, 789, Sala 5',         'RJ', '(21) 99876-5432', 'roberto.ferreira@email.com');

-- Veiculos
INSERT INTO veiculo (numero, placa, fabricante, modelo, anoModelo, qtdPortas, acessorios) VALUES
('VH001', 'ABC-1234', 'Volkswagen', 'Gol',     2022, 4, 'Ar-condicionado, Direção hidráulica, Vidro elétrico'),
('VH002', 'DEF-5678', 'Chevrolet',  'Onix',    2023, 4, 'Ar-condicionado, Central multimídia, Câmera de ré'),
('VH003', 'GHI-9012', 'Fiat',       'Argo',    2021, 4, 'Ar-condicionado, Sensor de estacionamento, Teto solar');

-- Alugueis
INSERT INTO aluguel (numero_veiculo, cpf_cliente, dataAluguel, dataentrega, entregue, observacao, valorPago) VALUES
('VH001', '123.456.789-00', '2026-05-01', '2026-05-05', 'S', 'Entrega sem avarias', 320.00),
('VH002', '987.654.321-00', '2026-05-10', '2026-05-15', 'S', 'Tanque cheio na devolucao', 450.00),
('VH003', '456.123.789-00', '2026-05-20', '2026-05-25', 'N', 'Cliente solicitou GPS adicional', NULL);

-- ============================================================
-- VERIFICACAO: SELECT * em cada tabela
-- ============================================================

SELECT '=== USUARIO ===' AS '';
SELECT * FROM usuario;

SELECT '=== CLIENTE ===' AS '';
SELECT * FROM cliente;

SELECT '=== VEICULO ===' AS '';
SELECT * FROM veiculo;

SELECT '=== ALUGUEL ===' AS '';
SELECT * FROM aluguel;
