import KakaoCallback from './KakaoCallback'

// 카카오가 인가 코드를 붙여 리다이렉트하는 페이지.
// 서버에서 searchParams(Next 16은 Promise)를 풀어 클라이언트로 code/error를 넘긴다.
export default async function KakaoCallbackPage({
  searchParams,
}: {
  searchParams: Promise<{ code?: string; error?: string }>
}) {
  const { code, error } = await searchParams
  return <KakaoCallback code={code} error={error} />
}
