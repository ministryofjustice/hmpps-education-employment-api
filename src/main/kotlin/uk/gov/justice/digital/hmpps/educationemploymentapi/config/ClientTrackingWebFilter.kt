package uk.gov.justice.digital.hmpps.educationemploymentapi.config
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.trace.Span
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import java.text.ParseException

@Component
class ClientTrackingWebFilter : WebFilter {
  private val bearer = "Bearer "
  private val clientIdKey = AttributeKey.stringKey("clientId")
  private val usernameKey = AttributeKey.stringKey("username")

  override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
    val token = exchange.request.headers.getFirst(HttpHeaders.AUTHORIZATION)
    if (token?.startsWith(bearer, ignoreCase = true) == true) {
      try {
        val jwtBody = getClaimsFromJWT(token)
        val currentSpan = Span.current()
        val user = jwtBody.getClaim("user_name")
        user?.run { currentSpan.setAttribute(usernameKey, user.toString()) }
        currentSpan.setAttribute(clientIdKey, jwtBody.getClaim("client_id").toString())
      } catch (e: ParseException) {
        log.warn("problem decoding jwt public key for application insights", e)
      }
    }
    return chain.filter(exchange)
  }

  private fun getClaimsFromJWT(token: String): JWTClaimsSet =
    SignedJWT.parse(token.replace(bearer, "")).jwtClaimsSet

  private companion object {
    private val log = LoggerFactory.getLogger(ClientTrackingWebFilter::class.java)
  }
}
