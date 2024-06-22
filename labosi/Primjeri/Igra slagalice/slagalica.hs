-- |
-- Sveučilište u Zagrebu, Fakultet elektrotehnike i računarstva
-- Umjetna inteligencija
--
-- Pretraživanje prostora stanja: Igra slagalice 
-- v1.0
--
-- Copyright: (c) 2011 Jan Snajder <jan.snajder@fer.hr>
--
-- Zadatak:
-- Napišite program za rješavanje slagalice proizvoljnih dimenzija 
-- uporabom algoritma A* i odgovarajuće heurističke funkcije. Početna 
-- pozicija slagalice neka se učitava iz datoteke (definirajte dva primjera 
-- nad kojima ćete demonstirati rad svog programa). Počevši od zadane 
-- pozicije, program treba pronaći i ispisati niz poteza koji dovodi do 
-- rješenja, odnosno prekinuti rad ako rješenje nije pronađeno ili ako su 
-- iscrpljeni računalni resursi. Dopuštenu potrošnju računalnih resursa 
-- (prostornih i vremenskih, npr. broj pohranjenih čvorova i broj ukupno 
-- proširenih čvorova) neka definira korisnik pri pokretanju programa.
-- Detaljnije: http://www.cut-the-knot.org/pythagoras/fifteen.shtml
--
------------------------------------------------------------------------------

import Data.List
import Data.Maybe
import Data.Array
import Data.Ord
import qualified Data.Set as S
import Control.Monad
import Text.Printf

------------------------------------------------------------------------------
-- Prikaz slagalice
------------------------------------------------------------------------------

-- Podatkovna struktura za prikaz slagalice. Radi učinkovitosti, 
-- koristi se cjelobrojno dvodimenzijsko polje, a dodatno se pohranjuje i
-- pozicija praznog mjesta. Prazno mjesto u polju prikazano je
-- vrijednošću '0'.
data Puzzle = Puzzle {
  unPuzzle :: Array (Int,Int) Int, 
  hole     :: (Int,Int) }
  deriving (Eq,Ord,Show)

-- Pomoćna funkcija (omotač) za izgradnju slagalice iz ugniježđene
-- liste (ne provjerava se ispravnost liste).
puzzle :: [[Int]] -> Puzzle
puzzle xxs = Puzzle { 
  unPuzzle = listArray ((0,0),(n-1,m-1)) (concat xxs),
  hole     = (i,j) }
  where l = concat xxs
        n = length xxs
        m = length $ head xxs
        Just i = findIndex (elem 0) xxs
        Just j = findIndex (==0) (xxs!!i)

-- Pretvorba slagalice u ugniježđenu listu.
toList :: Puzzle -> [[Int]]
toList p = chunk (m+1) xs
  where (_,(_,m)) = bounds $ unPuzzle p
        xs = elems $ unPuzzle p

-- Pomoćna funkcija za grupiranje liste u podliste zadane duljine.
chunk :: Int -> [a] -> [[a]]
chunk _ [] = []
chunk n xs = ys : chunk n zs
  where (ys,zs) = splitAt n xs

-- Funkcija za ispis slagalice. Prazno mjesto ispisuje se kao bjelina.
showPuzzle :: Puzzle -> String
showPuzzle = unlines . map (concatMap print) . toList 
  where print 0 = "   "
        print x = printf "%3d" x

------------------------------------------------------------------------------
-- Funkcija sljedbenika i ispitni predikat
------------------------------------------------------------------------------

-- Pomoćna funkcija koja zamjenjuje dva elementa polja na zadanim
-- pozicijama.
arraySwap :: (Ix i) => i -> i -> Array i e -> Array i e
arraySwap i1 i2 a = a // [(i1,a!i2),(i2,a!i1)]

-- Pomiče prazno mjesto u slagalici (prazno mjesto na poziciji (i,j)
-- zamjenjuje s pločicom na poziciji (i+x,j+y)).
moveHole :: Puzzle -> (Int,Int) -> Maybe Puzzle
moveHole p (x,y)
  | inBounds  = Just $ Puzzle { 
      unPuzzle = arraySwap (i,j) (i',j') (unPuzzle p), hole = (i',j') }
  | otherwise = Nothing
  where (i,j)     = hole p
        (i',j')   = (i+x,j+y)
        (_,(n,m)) = bounds $ unPuzzle p
        inBounds  = i' >= 0 && j' >=0 && i' <= n && j' <= m

-- Funkcija za generiranje valjanih sljedbenika zadane slagalice.
nextPuzzles :: Puzzle -> [Puzzle]
nextPuzzles p = mapMaybe (moveHole p) [(-1,0),(1,0),(0,-1),(0,1)]

-- Ispitni predikat; vraća True za ciljno stanje (prazno mjesto je
-- u doljnjem desnom kutu).
isSolved :: Puzzle -> Bool
isSolved p = l == [1..length l - 1] ++ [0]
  where l = elems $ unPuzzle p

------------------------------------------------------------------------------
-- Heurističke funkcije
------------------------------------------------------------------------------

-- Heuristika 1: broj pločica koje nisu na svome mjestu (prazno mjesto
-- se ne broji). Ova heuristika dobivena je relaksacijom problema,
-- stoga je nužno konzistentna.
heuristics1 :: Puzzle -> Int
heuristics1 = 
  length . filter (==False) . zipWith (==) [1..] . init . elems . unPuzzle

-- Heuristika 2: zbroj L1-udaljenosti svake pločice od njezinog mjesta 
-- (prazno mjesto se ne broji). Ova heuristika dobivena je
-- relaksacijom problema, stoga je nužno konzistentna.
heuristics2 :: Puzzle -> Int
heuristics2 p = sum $ map (\(ij,x) -> l1 (pos x) ij) xs
  where l1 (i1,j1) (i2,j2) = abs (i1-i2) + abs (j1-j2)
        pos x = ((x-1) `div` (m+1), (x-1) `mod` (m+1))
        (_,(n,m)) = bounds $ unPuzzle p
        xs = filter ((/=0).snd) . assocs $ unPuzzle p

------------------------------------------------------------------------------
-- Algoritam A*
------------------------------------------------------------------------------

newtype Node a = Node (a,Int,[a]) deriving (Eq,Show)

instance (Ord a) => Ord (Node a) where
  --Node (_,g1,_) <= Node (_,g2,_) = g1 <= g2
  Node (s1,_,_) <= Node (s2,_,_) = s1 <= s2

-- Usmjereno pretraživanje A* s listom posjećenih stanja. Funkcija
-- uzima početno stanje, funkciju sljedbenika, ispitni predikat,
-- heurističku funkciju i maksimalno dozvoljeni broj posjećenih
-- čvorova.  Funkcija vraća listu stanja od početnog do ciljnog
-- stanja, odnosno praznu listu, ako rješenje nije pronađeno. Ako
-- dosegne maksimalno dopušten broj čvorova, funkcija vraća
-- odgovarajuću poruku. Pretpostavke: (1) heuristika je konzistentna
-- (zato pamtimo samo posjećena stanja, a ne i zatvorene čvorove), (2)
-- cijena prijelaza je konstantna.
aStar :: (Ord a) => 
  a -> (a -> [a]) -> (a -> Bool) -> (a -> Int) -> Int -> Either String [a] 
aStar s0 succ goal h maxn = step [(s0,0,[])] S.empty 1
  where step [] v n      = Right []
        step (x@(s,g,ss):xs) v n
          | n > maxn     = Left  $ "Node limit " ++ show maxn ++ " exceeded"
          | goal s       = Right $ reverse (s:ss)
          | S.member s v = step xs v n
          | otherwise    = let es   = expand x
                               open = insertManyBy (comparing f) es xs
                           in  step open (S.insert s v) (n+1)
        expand (s,g,ss) = [(t,g+1,s:ss) | t <- succ s]
        f (s,g,_) = g + h s

-- Pomoćna funkcija: ubacuje elemente prve liste u drugu poštujući
-- zadani uređaj.
insertManyBy :: (a -> a -> Ordering) -> [a] -> [a] -> [a]
insertManyBy f xs ys = foldl (flip $ insertBy f) ys xs

-- Npr.:
-- > aStar puzzle33 nextPuzzles isSolved heuristics2 200
-- NB: Uz zadano ograničenje od 200 posjećenih čvorova, ovu slagalicu nije 
-- moguće rješiti pomoću heuristics1.

-- Ispis rješenja za zadanu slagalicu, zadanu heuristiku i zadano ograničenje 
-- broja posjećenih čvorova.
solve :: Puzzle -> (Puzzle -> Int) -> Int -> IO ()
solve p h maxn = case aStar p nextPuzzles isSolved h maxn of
  Left err -> putStrLn err
  Right ps -> forM_ (zip [1..] ps) (\(i,p) -> do
                putStrLn $ "Step " ++ show i ++ ":"
                putStrLn $ showPuzzle p)

-- Npr.
-- > solve puzzle33 heuristics2 200

------------------------------------------------------------------------------
-- Primjeri slagalica
------------------------------------------------------------------------------

puzzle22 :: Puzzle
puzzle22 = puzzle
  [[2,3],
   [0,1]]

puzzle23 :: Puzzle
puzzle23 = puzzle 
 [[2,3,0],
  [4,5,1]]

puzzle33 :: Puzzle
puzzle33 = puzzle 
  [[0,8,7],
   [6,5,4],
   [3,2,1]]

puzzle44 :: Puzzle
puzzle44 = puzzle 
  [[ 5,15, 1, 7],
   [ 9, 4, 2,11],
   [10,14, 6, 3],
   [13, 0,12, 8]]

